package com.example.managers;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages snowballs, regeneration, and related effects
 */
public class SnowballManager {
    private final JavaPlugin plugin;
    private final ScoreManager scoreManager;
    private final Set<UUID> snowballTrailActive = new HashSet<>();
    private final Map<UUID, BukkitTask> snowballRegenTasks = new HashMap<>();
    private boolean gameActive = false;
    
    public SnowballManager(JavaPlugin plugin, ScoreManager scoreManager) {
        this.plugin = plugin;
        this.scoreManager = scoreManager;
    }
    
    /**
     * Initialize for a new game
     */
    public void initialize() {
        gameActive = true;
        snowballTrailActive.clear();
        
        // Cancel any existing regen tasks
        for (BukkitTask task : snowballRegenTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        snowballRegenTasks.clear();
    }
    
    /**
     * Handle game ending
     */
    public void cleanup() {
        gameActive = false;
        
        // Cancel all snowball regen tasks
        for (BukkitTask task : snowballRegenTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        snowballRegenTasks.clear();
    }
    
    /**
     * Register a player
     */
    public void registerPlayer(Player player) {
        snowballTrailActive.add(player.getUniqueId());
    }
    
    /**
     * Handle player quitting
     */
    public void handlePlayerQuit(Player player) {
        BukkitTask regenTask = snowballRegenTasks.remove(player.getUniqueId());
        if (regenTask != null) {
            regenTask.cancel();
        }
        snowballTrailActive.remove(player.getUniqueId());
    }
    
    /**
     * Give initial snowballs to a player
     */
    public void giveSnowballs(Player player) {
        // Clear inventory first
        player.getInventory().clear();
        
        // Create a stack of snowballs with custom name
        ItemStack snowballs = new ItemStack(Material.SNOWBALL, 16);
        ItemMeta meta = snowballs.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Battle Snowball");
        snowballs.setItemMeta(meta);
        
        // Give snowballs in one slot only
        player.getInventory().setItem(0, snowballs);
    }
    
    /**
     * Remove all snowballs from a player
     */
    public void removeSnowballs(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.SNOWBALL) {
                player.getInventory().setItem(i, null);
            }
        }
    }
    
    /**
     * Start snowball regeneration for a player
     */
    public void startSnowballRegeneration(Player player) {
        // Cancel existing regen task if exists
        BukkitTask existingTask = snowballRegenTasks.remove(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }
        
        // Start a new regeneration task
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive || !player.isOnline()) {
                    cancel();
                    snowballRegenTasks.remove(player.getUniqueId());
                    return;
                }
                
                // Count current snowballs
                int currentSnowballs = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.SNOWBALL) {
                        currentSnowballs += item.getAmount();
                    }
                }
                
                // If player has less than 16 snowballs, give them one more
                if (currentSnowballs < 16) {
                    ItemStack snowballSlot = player.getInventory().getItem(0);
                    
                    if (snowballSlot != null && snowballSlot.getType() == Material.SNOWBALL) {
                        // Add to existing stack
                        snowballSlot.setAmount(snowballSlot.getAmount() + 1);
                    } else {
                        // Create a new stack
                        ItemStack newSnowball = new ItemStack(Material.SNOWBALL, 1);
                        ItemMeta meta = newSnowball.getItemMeta();
                        meta.setDisplayName(ChatColor.AQUA + "Battle Snowball");
                        newSnowball.setItemMeta(meta);
                        
                        player.getInventory().setItem(0, newSnowball);
                    }
                    
                    // Play quiet pickup sound when regenerating
                    if (currentSnowballs == 0) {
                        // First snowball after empty - make more noticeable
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                    } else if (currentSnowballs % 4 == 0) {
                        // Play sound every 4 snowballs
                        player.playSound(player.getLocation(), Sound.BLOCK_SNOW_STEP, 0.3f, 1.5f);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // 20 ticks = 1 second
        
        snowballRegenTasks.put(player.getUniqueId(), task);
    }
    
    /**
     * Handle a snowball being thrown
     */
    public void handleSnowballThrow(ProjectileLaunchEvent event) {
        if (!gameActive || !(event.getEntity() instanceof Snowball)) {
            return;
        }
        
        Snowball snowball = (Snowball) event.getEntity();
        if (snowball.getShooter() instanceof Player) {
            Player shooter = (Player) snowball.getShooter();
            
            if (snowballTrailActive.contains(shooter.getUniqueId())) {
                // Create snowball trail
                new BukkitRunnable() {
                    int ticks = 0;
                    
                    @Override
                    public void run() {
                        if (snowball.isDead() || !snowball.isValid() || ticks > 100) {
                            cancel();
                            return;
                        }
                        
                        // Determine particle color based on player score
                        int score = scoreManager.getScore(shooter);
                        Color color;
                        
                        if (score >= scoreManager.getHitsToWin() - 1) {
                            // Near victory - gold
                            color = Color.fromRGB(255, 215, 0);
                        } else if (score >= scoreManager.getHitsToWin() - 3) {
                            // Getting close - orange
                            color = Color.fromRGB(255, 165, 0);
                        } else {
                            // Regular - white/blue
                            color = Color.fromRGB(173, 216, 230);
                        }
                        
                        // Spawn particle trail
                        snowball.getWorld().spawnParticle(
                            Particle.REDSTONE, 
                            snowball.getLocation(), 
                            3, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(color, 1)
                        );
                        
                        ticks++;
                    }
                }.runTaskTimer(plugin, 1L, 1L);
            }
        }
    }
}
