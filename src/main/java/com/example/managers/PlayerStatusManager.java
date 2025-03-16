package com.example.managers;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerStatusManager {
    private final JavaPlugin plugin;
    
    public PlayerStatusManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startStatusUpdateTask();
    }
    
    /**
     * Sets a player to adventure mode and ensures they have full health and hunger
     */
    public void updatePlayerStatus(Player player) {
        // Set to adventure mode
        player.setGameMode(GameMode.ADVENTURE);
        
        // Set full health
        player.setHealth(player.getMaxHealth());
        
        // Set full food level (20 is max)
        player.setFoodLevel(20);
        
        // Remove hunger effect
        player.setSaturation(20);
        player.setExhaustion(0);
    }
    
    /**
     * Updates all online players' status
     */
    public void updateAllPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayerStatus(player);
        }
    }
    
    /**
     * Removes monsters in all worlds
     */
    public void removeMonsters() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (Entity entity : player.getWorld().getEntities()) {
                if (entity instanceof Monster) {
                    entity.remove();
                }
            }
        }
    }
    
    /**
     * Starts a task to periodically update player status and remove monsters
     */
    private void startStatusUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayers();
                removeMonsters();
            }
        }.runTaskTimer(plugin, 20L, 200L); // Run every 10 seconds (200 ticks)
    }
}
