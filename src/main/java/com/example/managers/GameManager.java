package com.example.managers;

import com.example.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Manages the overall game state and coordinates between other managers
 */
public class GameManager {
    private final JavaPlugin plugin;
    private final LocationUtil locationUtil;
    private final PlayerStatusManager playerStatusManager;
    private final ScoreManager scoreManager;
    private final BoundaryManager boundaryManager;
    private final SnowballManager snowballManager;
    private final EffectsManager effectsManager;
    private final ThreatTracker threatTracker;
    
    private boolean gameActive = false;
    private boolean countdownActive = false;
    
    public GameManager(JavaPlugin plugin, LocationUtil locationUtil, PlayerStatusManager playerStatusManager) {
        this.plugin = plugin;
        this.locationUtil = locationUtil;
        this.playerStatusManager = playerStatusManager;
        
        // Initialize managers
        this.scoreManager = new ScoreManager(plugin);
        this.boundaryManager = new BoundaryManager(plugin, locationUtil, scoreManager);
        this.effectsManager = new EffectsManager(plugin);
        this.snowballManager = new SnowballManager(plugin, scoreManager);
        this.threatTracker = new ThreatTracker(plugin, scoreManager, effectsManager);
    }
    
    public boolean isGameActive() {
        return gameActive;
    }
    
    public boolean isCountdownActive() {
        return countdownActive;
    }
    
    /**
     * Starts a new game
     */
    public void startGame(int hitsToWin, double boundaryDistance) {
        // Check if all required locations are set
        if (!locationUtil.areAllLocationsSet()) {
            plugin.getLogger().warning("Cannot start game: Not all required locations are set!");
            return;
        }
        
        // Check if there are enough players
        if (plugin.getServer().getOnlinePlayers().size() < 2) {
            plugin.getLogger().warning("Cannot start game: Need at least 2 players!");
            return;
        }
        
        // Check if there are enough spawn points
        if (locationUtil.getSpawnPoints().size() < plugin.getServer().getOnlinePlayers().size()) {
            plugin.getLogger().warning("Cannot start game: Not enough spawn points for all players!");
            return;
        }
        
        // Initialize managers
        scoreManager.initializeScoreboard(hitsToWin, boundaryDistance);
        boundaryManager.initializeBoundary(boundaryDistance);
        snowballManager.initialize();
        
        // Add all online players to the game
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            scoreManager.registerPlayer(player);
            snowballManager.registerPlayer(player);
        }
        
        // Update player status
        playerStatusManager.updateAllPlayers();
        
        // Remove monsters
        playerStatusManager.removeMonsters();
        
        // Teleport players to spawn points
        teleportPlayersToSpawnPoints();
        
        // Start countdown
        startCountdown();
        
        gameActive = true;
        
        // Announce game start with cool particles in the sky
        Location middle = locationUtil.getMiddlePoint();
        if (middle != null) {
            effectsManager.createGameStartEffect(middle);
        }
        
        // Broadcast boundary information
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(ChatColor.YELLOW + "Boundary distance: " + 
                              ChatColor.WHITE + (int)boundaryDistance + 
                              ChatColor.YELLOW + " blocks from the middle.");
        }
    }
    
    /**
     * Teleports players to spawn points
     */
    private void teleportPlayersToSpawnPoints() {
        List<Location> spawnPoints = new ArrayList<>(locationUtil.getSpawnPoints());
        Collections.shuffle(spawnPoints);
        
        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            
            // Find a spawn point at least 5 blocks away from other players
            Location spawnLocation = findValidSpawnLocation(spawnPoints, players, i);
            
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
                boundaryManager.updateLastValidLocation(player, spawnLocation);
            } else {
                plugin.getLogger().warning("Could not find a valid spawn location for " + player.getName());
            }
        }
    }
    
    /**
     * Finds a valid spawn location for a player
     */
    private Location findValidSpawnLocation(List<Location> spawnPoints, List<Player> players, int currentPlayerIndex) {
        Player currentPlayer = players.get(currentPlayerIndex);
        
        for (Location spawnLocation : spawnPoints) {
            boolean isValid = true;
            
            // Check if this spawn point is at least 5 blocks away from other already spawned players
            for (int j = 0; j < currentPlayerIndex; j++) {
                Player otherPlayer = players.get(j);
                
                // Calculate distance only in X and Z (horizontal plane)
                double dx = otherPlayer.getLocation().getX() - spawnLocation.getX();
                double dz = otherPlayer.getLocation().getZ() - spawnLocation.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                
                if (distance < 5) {
                    isValid = false;
                    break;
                }
            }
            
            if (isValid) {
                return spawnLocation;
            }
        }
        
        // If no valid spawn point is found, just use the first available one
        if (!spawnPoints.isEmpty()) {
            return spawnPoints.get(0);
        }
        
        return null;
    }
    
    /**
     * Starts the countdown before the game
     */
    private void startCountdown() {
        countdownActive = true;
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendTitle(ChatColor.GOLD + "Game Starting", ChatColor.WHITE + "Prepare for Snowball Fight!", 10, 70, 20);
        }
        
        effectsManager.startCountdownEffect(() -> {
            countdownActive = false;
            
            // Give players snowballs and start snowball regeneration
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                snowballManager.giveSnowballs(player);
                snowballManager.startSnowballRegeneration(player);
            }
        });
    }
    
    /**
     * Ends the current game
     */
    public void endGame() {
        if (!gameActive) {
            return;
        }
        
        // Find the winner
        Player winner = scoreManager.getWinner();
        int highestScore = scoreManager.getHighestScore();
        
        // Clean up tracking
        threatTracker.cleanup();
        snowballManager.cleanup();
        
        // Announce the winner
        if (winner != null) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(
                    ChatColor.GOLD + winner.getName() + " Wins!",
                    ChatColor.WHITE + "With " + highestScore + " points!",
                    10, 70, 20
                );
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
            
            // Victory effects
            effectsManager.createVictoryFireworks(winner);
            
            // Teleport winner to winner spot, others to loser spot
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.equals(winner)) {
                    player.teleport(locationUtil.getWinnerSpot());
                } else {
                    player.teleport(locationUtil.getLoserSpot());
                }
            }
        } else {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.RED + "Game ended without a winner!");
            }
        }
        
        // Clean up game state
        gameActive = false;
        
        // Remove snowballs from players' inventories
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            snowballManager.removeSnowballs(player);
        }
        
        // Clean up scoreboard and nametags
        scoreManager.cleanupScoreboard();
        
        // Update player status
        playerStatusManager.updateAllPlayers();
        
        // Remove monsters
        playerStatusManager.removeMonsters();
    }
    
    /**
     * Handles a new player joining
     */
    public void handlePlayerJoin(Player player) {
        // Ensure player is in adventure mode with full health/hunger
        playerStatusManager.updatePlayerStatus(player);
        
        if (gameActive) {
            // Register player with managers
            scoreManager.registerPlayer(player);
            snowballManager.registerPlayer(player);
            
            // Teleport player to a random spawn point
            if (!countdownActive) {
                teleportPlayerToRandomSpawn(player);
                snowballManager.giveSnowballs(player);
                snowballManager.startSnowballRegeneration(player);
            }
        }
    }
    
    /**
     * Handles a player quitting
     */
    public void handlePlayerQuit(Player player) {
        // Clean up player in all managers
        scoreManager.handlePlayerQuit(player);
        boundaryManager.handlePlayerQuit(player);
        snowballManager.handlePlayerQuit(player);
        threatTracker.handlePlayerQuit(player);
    }
    
    /**
     * Handles player movement
     */
    public void handlePlayerMove(Player player, Location to) {
        if (!gameActive) return;
        
        if (boundaryManager.handlePlayerMove(player, to, countdownActive)) {
            // Player needs to be teleported to a spawn point (outside boundary)
            teleportPlayerToRandomSpawn(player);
        }
    }
    
    /**
     * Handles a snowball hit
     */
    public void handleSnowballHit(Player thrower, Player hit) {
        if (!gameActive || thrower.equals(hit)) {
            return;
        }
        
        // Check if the hit player was one away from winning
        boolean wasNearWin = scoreManager.isPlayerNearWin(hit);
        
        // If hit player was a threat (1 away from winning), decrease their score
        if (wasNearWin) {
            scoreManager.decrementScore(hit);
            
            // No text message - just play sound effect
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 1.0f);
            }
            
            // Stop tracking this player
            threatTracker.stopTracking(hit);
        }
        
        // Increment thrower's score
        scoreManager.incrementScore(thrower);
        
        // Play sound effects for the hit - No chat message for regular hits
        hit.playSound(hit.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
        thrower.playSound(thrower.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        effectsManager.playHitEffects(hit);
        
        // First teleport player high in the air above middle, then to spawn point after delay
        Location middle = locationUtil.getMiddlePoint();
        final Location highLocation = new Location(
            middle.getWorld(), 
            middle.getX(), 
            middle.getY() + 100, // 100 blocks above middle
            middle.getZ()
        );
        
        // Teleport player high in the air
        hit.teleport(highLocation);
        
        // Add falling trail
        effectsManager.createFallingTrail(hit);
        
        // Then teleport to spawn point after a short delay (20 ticks = 1 second)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hit.isOnline() && gameActive) {
                    teleportPlayerToRandomSpawn(hit);
                    
                    // Ensure player has full health and hunger
                    playerStatusManager.updatePlayerStatus(hit);
                    
                    // Give snowballs again
                    snowballManager.giveSnowballs(hit);
                    
                    // Restart their snowball regeneration
                    snowballManager.startSnowballRegeneration(hit);
                    
                    // Add respawn effect
                    effectsManager.createRespawnEffect(hit);
                }
            }
        }.runTaskLater(plugin, 20L);
        
        // Check if the thrower is one away from winning
        if (scoreManager.isPlayerNearWin(thrower) && !threatTracker.isTracking(thrower)) {
            // Only minimal effects when first reaching one-away status
            threatTracker.startTracking(thrower);
        } else if (!scoreManager.isPlayerNearWin(thrower) && threatTracker.isTracking(thrower)) {
            // If already tracking this player and they're no longer a threat, stop tracking
            threatTracker.stopTracking(thrower);
        }
        
        // Check for winner
        if (scoreManager.hasPlayerWon(thrower)) {
            // Just one minimal message for win
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.GOLD + "★ " + 
                                  ChatColor.GREEN + thrower.getName() + 
                                  ChatColor.GOLD + " has won! ★");
            }
            
            // Delay ending the game to allow for the visual effects
            Bukkit.getScheduler().runTaskLater(plugin, this::endGame, 40L); // 2 seconds delay
        }
    }
    
    /**
     * Handles a snowball being thrown
     */
    public void handleSnowballThrow(ProjectileLaunchEvent event) {
        snowballManager.handleSnowballThrow(event);
    }
    
    /**
     * Teleports a player to a random spawn point
     */
    private void teleportPlayerToRandomSpawn(Player player) {
        List<Location> spawnPoints = new ArrayList<>(locationUtil.getSpawnPoints());
        if (spawnPoints.isEmpty()) {
            return;
        }
        
        Collections.shuffle(spawnPoints);
        
        // Try to find a spawn point at least 5 blocks away from other players
        for (Location spawnLocation : spawnPoints) {
            boolean isFarEnough = true;
            
            for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                if (!otherPlayer.equals(player)) {
                    // Only check X and Z distance (horizontal plane)
                    double dx = otherPlayer.getLocation().getX() - spawnLocation.getX();
                    double dz = otherPlayer.getLocation().getZ() - spawnLocation.getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    
                    if (distance < 5) {
                        isFarEnough = false;
                        break;
                    }
                }
            }
            
            if (isFarEnough) {
                player.teleport(spawnLocation);
                boundaryManager.updateLastValidLocation(player, spawnLocation);
                return;
            }
        }
        
        // If no suitable point is found, just use the first one
        player.teleport(spawnPoints.get(0));
        boundaryManager.updateLastValidLocation(player, spawnPoints.get(0));
    }
    
    /**
     * Shows the current scores to all players
     */
    public void showScoreboard() {
        if (!gameActive) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.RED + "No game is currently active!");
            }
            return;
        }
        
        scoreManager.showScores();
    }
}