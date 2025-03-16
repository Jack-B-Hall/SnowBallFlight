package com.example.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks and highlights players who are close to winning
 */
public class ThreatTracker {
    private final JavaPlugin plugin;
    private final ScoreManager scoreManager;
    private final EffectsManager effectsManager;
    private final Map<UUID, BukkitRunnable> threatTrackers = new HashMap<>();
    private final Map<UUID, Long> lastNotificationTime = new HashMap<>();
    private static final long NOTIFICATION_COOLDOWN = 10000; // 10 seconds in milliseconds
    
    public ThreatTracker(JavaPlugin plugin, ScoreManager scoreManager, EffectsManager effectsManager) {
        this.plugin = plugin;
        this.scoreManager = scoreManager;
        this.effectsManager = effectsManager;
    }
    
    /**
     * Clean up when game ends
     */
    public void cleanup() {
        // Remove glowing effect from all threat players
        for (UUID playerId : threatTrackers.keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removeGlowingEffect(player);
            }
        }
        
        // Cancel all trackers
        for (BukkitRunnable task : threatTrackers.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        threatTrackers.clear();
        lastNotificationTime.clear();
    }
    
    /**
     * Clean up when a player quits
     */
    public void handlePlayerQuit(Player player) {
        BukkitRunnable threatTask = threatTrackers.remove(player.getUniqueId());
        if (threatTask != null) {
            threatTask.cancel();
        }
        lastNotificationTime.remove(player.getUniqueId());
        
        // Remove effects
        removeGlowingEffect(player);
    }
    
    /**
     * Apply glowing effect to player
     */
    private void applyGlowingEffect(Player player) {
        // Apply glowing effect (indefinite duration but will be removed when tracking ends)
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.GLOWING, 
            Integer.MAX_VALUE, // Effectively permanent until removed
            0, // Effect level 0 (normal)
            false, // No particles
            false, // No icon
            true  // Show to everyone
        ));
    }
    
    /**
     * Remove glowing effect from player
     */
    private void removeGlowingEffect(Player player) {
        player.removePotionEffect(PotionEffectType.GLOWING);
    }
    
    /**
     * Start tracking a player who is close to winning
     */
    public void startTracking(Player player) {
        // Initial notification only
        sendInitialNotification(player);
        
        // Apply glowing effect to make them stand out visually
        applyGlowingEffect(player);
        
        // Update scoreboard to mark player as a threat
        scoreManager.markThreatPlayer(player);
        
        // Cancel existing tracker if there is one
        BukkitRunnable existingTracker = threatTrackers.remove(player.getUniqueId());
        if (existingTracker != null) {
            existingTracker.cancel();
        }
        
        // Create a new threat tracker
        BukkitRunnable threatTracker = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !scoreManager.isPlayerNearWin(player)) {
                    cancel();
                    threatTrackers.remove(player.getUniqueId());
                    lastNotificationTime.remove(player.getUniqueId());
                    scoreManager.unmarkThreatPlayer(player);
                    removeGlowingEffect(player);
                    return;
                }
                
                // Create visual indicators
                long currentTime = System.currentTimeMillis();
                Long lastTime = lastNotificationTime.get(player.getUniqueId());
                if (lastTime == null || (currentTime - lastTime > NOTIFICATION_COOLDOWN)) {
                    // Create cluster of fireworks around player's location to make them highly visible
                    effectsManager.createWarningFireworks(player.getLocation());
                    
                    // Create a red circle at player's feet
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                        double x = Math.cos(angle) * 2;
                        double z = Math.sin(angle) * 2;
                        Location particleLoc = player.getLocation().clone().add(x, 0.1, z);
                        
                        player.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            particleLoc,
                            2, 0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0f)
                        );
                    }
                    
                    // Play sound
                    player.getWorld().playSound(player.getLocation(), 
                                               Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 
                                               0.5f, 1.2f);
                    
                    lastNotificationTime.put(player.getUniqueId(), currentTime);
                }
            }
        };
        
        // Run the task every 5 seconds
        threatTracker.runTaskTimer(plugin, 0L, 100L);
        threatTrackers.put(player.getUniqueId(), threatTracker);
    }
    
    /**
     * Send a one-time notification when player first becomes a threat
     */
    private void sendInitialNotification(Player threatPlayer) {
        // Play a sound
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.8f);
        }
        
        // Only send one chat message
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(ChatColor.RED + "⚠ " + 
                         ChatColor.YELLOW + threatPlayer.getName() +
                         ChatColor.RED + " is one hit away from winning!");
        }
    }
    
    /**
     * Stop tracking a player
     */
    public void stopTracking(Player player) {
        // Cancel the tracker
        BukkitRunnable task = threatTrackers.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        // Remove glowing effect
        removeGlowingEffect(player);
        
        // Clean up other resources
        lastNotificationTime.remove(player.getUniqueId());
        scoreManager.unmarkThreatPlayer(player);
    }
    
    /**
     * Check if a player is being tracked
     */
    public boolean isTracking(Player player) {
        return threatTrackers.containsKey(player.getUniqueId());
    }
}