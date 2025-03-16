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
        
        // Create objective for the sidebar - Using empty string as criteria for updated versions
        try {
            // For 1.13+ servers
            objective = scoreboard.registerNewObjective("snowballfight", "dummy", ChatColor.AQUA + "Snow Ball Fight");
        } catch (Exception e) {
            // Fallback for older servers (though the plugin targets 1.20.1)
            objective = scoreboard.registerNewObjective("snowballfight", "dummy");
            objective.setDisplayName(ChatColor.AQUA + "Snow Ball Fight");
        }
        
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
        // Make sure objective is using the right display slot
        if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Clear existing scores to rebuild the scoreboard
        for (String entry : new ArrayList<>(scoreboard.getEntries())) {
            scoreboard.resetScores(entry);
        }
        
        // Create a list to track entries in order
        List<String> entries = new ArrayList<>();
        Map<String, Integer> scores = new HashMap<>();
        
        // Add target score line at the top
        String targetLine = ChatColor.GOLD + "Target: " + ChatColor.WHITE + hitsToWin + " hits";
        entries.add(targetLine);
        scores.put(targetLine, 15);
        
        // Add separator line
        String separatorLine = ChatColor.GRAY + "---------------";
        entries.add(separatorLine);
        scores.put(separatorLine, 14);
        
        // Get a sorted list of players by score
        List<Player> sortedPlayers = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sortedPlayers.add(player);
        }
        
        // Sort in descending order
        sortedPlayers.sort((p1, p2) -> {
            int score1 = playerScores.getOrDefault(p1.getUniqueId(), 0);
            int score2 = playerScores.getOrDefault(p2.getUniqueId(), 0);
            return Integer.compare(score2, score1); // Descending order
        });
        
        // Add players with scores
        int scorePosition = 13; // Starting position after headers
        for (Player player : sortedPlayers) {
            UUID playerId = player.getUniqueId();
            
            // Create display name based on threat status
            String displayName;
            if (threatPlayers.contains(playerId)) {
                displayName = ChatColor.RED + "⚠ " + ChatColor.AQUA + player.getName();
            } else {
                displayName = ChatColor.AQUA + player.getName();
            }
            
            // Create score display
            String scoreEntry = displayName + ChatColor.WHITE + ": " + playerScores.getOrDefault(playerId, 0);
            
            entries.add(scoreEntry);
            scores.put(scoreEntry, scorePosition--);
            playerDisplayNames.put(playerId, displayName);
            
            // Prevent going below 0
            if (scorePosition < 0) break;
        }
        
        // Apply all entries to scoreboard in order
        for (String entry : entries) {
            objective.getScore(entry).setScore(scores.get(entry));
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