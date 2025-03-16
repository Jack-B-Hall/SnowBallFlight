package com.example.managers;

import com.example.util.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Manages player movement boundaries and penalties
 */
public class BoundaryManager {
    private final JavaPlugin plugin;
    private final LocationUtil locationUtil;
    private final ScoreManager scoreManager;
    private final Map<UUID, Location> lastValidLocations = new HashMap<>();
    private final Set<UUID> warningGiven = new HashSet<>();
    private double boundaryDistance;
    
    public BoundaryManager(JavaPlugin plugin, LocationUtil locationUtil, ScoreManager scoreManager) {
        this.plugin = plugin;
        this.locationUtil = locationUtil;
        this.scoreManager = scoreManager;
    }
    
    /**
     * Initializes the boundary for a new game
     */
    public void initializeBoundary(double boundaryDistance) {
        this.boundaryDistance = boundaryDistance;
        lastValidLocations.clear();
        warningGiven.clear();
    }
    
    /**
     * Handles a player quitting - cleanup resources
     */
    public void handlePlayerQuit(Player player) {
        lastValidLocations.remove(player.getUniqueId());
        warningGiven.remove(player.getUniqueId());
    }
    
    /**
     * Handles player movement - enforces boundaries
     */
    public boolean handlePlayerMove(Player player, Location to, boolean countdownActive) {
        if (countdownActive) {
            // Cancel movement during countdown
            player.teleport(player.getLocation());
            return false;
        }
        
        // Check if player is outside boundary - only check X and Z (horizontal)
        Location middle = locationUtil.getMiddlePoint();
        double dx = to.getX() - middle.getX();
        double dz = to.getZ() - middle.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        
        if (distance > boundaryDistance + 10) {
            // Player is outside boundary - teleport back and penalize
            player.sendMessage(ChatColor.RED + "You have left the play area! -1 point penalty.");
            
            scoreManager.decrementScore(player);
            
            // Returning true signals the player needs to be teleported to a spawn point
            warningGiven.remove(player.getUniqueId());
            return true;
        } else if (distance > boundaryDistance && !warningGiven.contains(player.getUniqueId())) {
            // Player is approaching boundary - give warning
            player.sendMessage(ChatColor.YELLOW + "Warning: You are approaching the boundary!");
            
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            
            // Show boundary particle effect
            Location boundaryLoc = player.getLocation().clone();
            boundaryLoc.setY(boundaryLoc.getY() + 1);
            player.getWorld().spawnParticle(
                Particle.REDSTONE, 
                boundaryLoc, 
                30, 0.5, 0.5, 0.5, 0,
                new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1)
            );
            
            warningGiven.add(player.getUniqueId());
            lastValidLocations.put(player.getUniqueId(), player.getLocation());
            return false;
        } else if (distance <= boundaryDistance) {
            // Player is within bounds - update last valid location
            warningGiven.remove(player.getUniqueId());
            lastValidLocations.put(player.getUniqueId(), player.getLocation());
            return false;
        }
        
        return false;
    }
    
    /**
     * Gets the last valid location for a player
     */
    public Location getLastValidLocation(Player player) {
        return lastValidLocations.get(player.getUniqueId());
    }
    
    /**
     * Updates the last valid location for a player
     */
    public void updateLastValidLocation(Player player, Location location) {
        lastValidLocations.put(player.getUniqueId(), location);
    }
}
