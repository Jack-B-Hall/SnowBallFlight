package com.example.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * Manages player scores and the scoreboard display
 */
public class ScoreManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Integer> playerScores = new HashMap<>();
    private final Set<UUID> threatPlayers = new HashSet<>();
    private final Map<UUID, String> playerDisplayNames = new HashMap<>(); // Track display names
    private Scoreboard scoreboard;
    private Objective objective;
    private Team hideNametagTeam;
    private int hitsToWin;
    private double boundaryDistance;
    
    public ScoreManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initializes the scoreboard for a new game
     */
    public void initializeScoreboard(int hitsToWin, double boundaryDistance) {
        this.hitsToWin = hitsToWin;
        this.boundaryDistance = boundaryDistance;
        playerScores.clear();
        threatPlayers.clear();
        playerDisplayNames.clear(); // Clear display names
        
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        
        // Create objective for the sidebar
        objective = scoreboard.registerNewObjective("snowballfight", "dummy", ChatColor.AQUA + "Snow Ball Fight");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Create team for hiding nametags
        if (scoreboard.getTeam("hideNametags") != null) {
            scoreboard.getTeam("hideNametags").unregister();
        }
        
        hideNametagTeam = scoreboard.registerNewTeam("hideNametags");
        hideNametagTeam.setNameTagVisibility(NameTagVisibility.NEVER); // Hide nametags completely
        hideNametagTeam.setCanSeeFriendlyInvisibles(false);
        hideNametagTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER); // Prevent player collisions
    }
    
    /**
     * Get the scoreboard
     */
    public Scoreboard getScoreboard() {
        return scoreboard;
    }
    
    /**
     * Get the hide nametag team
     */
    public Team getHideNametagTeam() {
        return hideNametagTeam;
    }
    
    /**
     * Registers a player in the game
     */
    public void registerPlayer(Player player) {
        playerScores.putIfAbsent(player.getUniqueId(), 0);
        hideNametagTeam.addEntry(player.getName());
        updateScoreboard();
    }
    
    /**
     * Handles player quit - clean up resources
     */
    public void handlePlayerQuit(Player player) {
        hideNametagTeam.removeEntry(player.getName());
        threatPlayers.remove(player.getUniqueId());
        
        // Remove player's display name from scoreboard
        String oldDisplayName = playerDisplayNames.remove(player.getUniqueId());
        if (oldDisplayName != null && scoreboard != null) {
            scoreboard.resetScores(oldDisplayName);
        }
    }
    
    /**
     * Mark a player as a threat (one hit from winning)
     */
    public void markThreatPlayer(Player player) {
        threatPlayers.add(player.getUniqueId());
        updateScoreboard();
    }
    
    /**
     * Unmark a player as a threat
     */
    public void unmarkThreatPlayer(Player player) {
        threatPlayers.remove(player.getUniqueId());
        updateScoreboard();
    }
    
    /**
     * Updates the scoreboard for all players
     */
    public void updateScoreboard() {
        // Add target score line if not already there
        Score targetScore = objective.getScore(ChatColor.GOLD + "Target: " + ChatColor.WHITE + hitsToWin + " hits");
        targetScore.setScore(999); // High value to keep it at the top
        
        // Add separator
        Score separator = objective.getScore(ChatColor.GRAY + "---------------");
        separator.setScore(997);
        
        // Update all player scores
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            String oldDisplayName = playerDisplayNames.get(playerId);
            
            // Create new display name based on threat status
            String newDisplayName;
            if (threatPlayers.contains(playerId)) {
                newDisplayName = ChatColor.RED + "⚠ " + ChatColor.AQUA + player.getName();
            } else {
                newDisplayName = ChatColor.AQUA + player.getName();
            }
            
            // If display name has changed, remove the old entry
            if (oldDisplayName != null && !oldDisplayName.equals(newDisplayName)) {
                scoreboard.resetScores(oldDisplayName);
            }
            
            // Add the new entry and store the display name
            objective.getScore(newDisplayName).setScore(playerScores.getOrDefault(playerId, 0));
            playerDisplayNames.put(playerId, newDisplayName);
        }
        
        // Set scoreboard for all players
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.setScoreboard(scoreboard);
        }
    }
    
    /**
     * Updates scoreboard after a specific player's score changes
     */
    public void updateScoreboard(Player player) {
        updateScoreboard(); // Just call the full update for simplicity
    }
    
    /**
     * Shows current scores in chat - only accessible with command
     */
    public void showScores() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(ChatColor.GOLD + "=== Snow Ball Fight Scores ===");
            player.sendMessage(ChatColor.YELLOW + "Target: " + ChatColor.WHITE + hitsToWin + " hits");
            player.sendMessage(ChatColor.YELLOW + "Boundary: " + ChatColor.WHITE + (int)boundaryDistance + " blocks");
            player.sendMessage(ChatColor.GRAY + "---------------");
            
            for (Map.Entry<UUID, Integer> entry : playerScores.entrySet()) {
                Player scorePlayer = plugin.getServer().getPlayer(entry.getKey());
                if (scorePlayer != null) {
                    // Special formatting for threat players
                    String prefix = threatPlayers.contains(entry.getKey()) ? 
                                     ChatColor.RED + "⚠ " : "";
                    
                    player.sendMessage(prefix + ChatColor.AQUA + scorePlayer.getName() + ": " + 
                                       ChatColor.WHITE + entry.getValue() + " points");
                }
            }
            player.sendMessage(ChatColor.GOLD + "===========================");
        }
    }
    
    /**
     * Increments player score
     */
    public void incrementScore(Player player) {
        int currentScore = playerScores.getOrDefault(player.getUniqueId(), 0);
        playerScores.put(player.getUniqueId(), currentScore + 1);
        updateScoreboard();
    }
    
    /**
     * Decrements player score (never below 0)
     */
    public void decrementScore(Player player) {
        int currentScore = playerScores.getOrDefault(player.getUniqueId(), 0);
        // Never go below 0 (prevent underflow)
        playerScores.put(player.getUniqueId(), Math.max(0, currentScore - 1));
        updateScoreboard();
    }
    
    /**
     * Gets a player's current score
     */
    public int getScore(Player player) {
        return playerScores.getOrDefault(player.getUniqueId(), 0);
    }
    
    /**
     * Gets the player with the highest score
     */
    public Player getWinner() {
        UUID winnerUUID = null;
        int highestScore = -1;
        
        for (Map.Entry<UUID, Integer> entry : playerScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                winnerUUID = entry.getKey();
            }
        }
        
        return winnerUUID != null ? plugin.getServer().getPlayer(winnerUUID) : null;
    }
    
    /**
     * Gets the highest score
     */
    public int getHighestScore() {
        int highestScore = 0;
        for (int score : playerScores.values()) {
            if (score > highestScore) {
                highestScore = score;
            }
        }
        return highestScore;
    }
    
    /**
     * Cleans up scoreboard at game end
     */
    public void cleanupScoreboard() {
        // Remove players from the hide nametag team
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hideNametagTeam != null) {
                hideNametagTeam.removeEntry(player.getName());
            }
            
            // Clear the scoreboard
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        
        threatPlayers.clear();
        playerDisplayNames.clear(); // Clear the display name mapping
    }
    
    /**
     * Returns the number of hits needed to win
     */
    public int getHitsToWin() {
        return hitsToWin;
    }
    
    /**
     * Returns the boundary distance
     */
    public double getBoundaryDistance() {
        return boundaryDistance;
    }
    
    /**
     * Checks if a player has won
     */
    public boolean hasPlayerWon(Player player) {
        return getScore(player) >= hitsToWin;
    }
    
    /**
     * Checks if a player is one hit away from winning
     */
    public boolean isPlayerNearWin(Player player) {
        return getScore(player) == hitsToWin - 1;
    }
}