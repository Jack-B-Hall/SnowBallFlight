package com.example.managers;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Manages visual and sound effects for the game
 */
public class EffectsManager {
    private final JavaPlugin plugin;
    
    public EffectsManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates firework effect at the middle of the arena
     */
    public void createGameStartEffect(Location location) {
        // Create multiple fireworks
        for (int i = 0; i < 5; i++) {
            final int delay = i * 4;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                spawnRandomFirework(location.clone().add(
                    -5 + new Random().nextInt(11),  // Random x offset (-5 to +5)
                    5 + new Random().nextInt(6),    // Random height (5 to 10)
                    -5 + new Random().nextInt(11)   // Random z offset (-5 to +5)
                ));
            }, delay);
        }
        
        // Spawn firework particles in the sky
        location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, 
                                         location.clone().add(0, 10, 0), 
                                         100, 5, 5, 5, 0.1);
    }
    
    /**
     * Spawns a random colored firework
     */
    public void spawnRandomFirework(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        
        // Random Type
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        FireworkEffect.Type type = types[new Random().nextInt(types.length)];
        
        // Random Colors
        Color[] colors = {Color.RED, Color.BLUE, Color.WHITE, Color.AQUA, Color.FUCHSIA, 
                         Color.LIME, Color.YELLOW, Color.ORANGE, Color.PURPLE};
        Color primaryColor = colors[new Random().nextInt(colors.length)];
        Color fadeColor = colors[new Random().nextInt(colors.length)];
        
        // Create effect
        FireworkEffect effect = FireworkEffect.builder()
            .withColor(primaryColor)
            .withFade(fadeColor)
            .with(type)
            .trail(new Random().nextBoolean())
            .flicker(new Random().nextBoolean())
            .build();
        
        meta.addEffect(effect);
        meta.setPower(1); // Lower power for quicker explosion
        firework.setFireworkMeta(meta);
    }
    
    /**
     * Spawns a bright red warning firework
     */
    public void spawnWarningFirework(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        
        // Create bright red warning effect
        FireworkEffect effect = FireworkEffect.builder()
            .withColor(Color.RED)
            .withFade(Color.ORANGE)
            .with(FireworkEffect.Type.BURST)
            .trail(true)
            .flicker(true)
            .build();
        
        meta.addEffect(effect);
        meta.setPower(0); // Immediate explosion
        firework.setFireworkMeta(meta);
    }
    
    /**
     * Creates a cluster of warning fireworks
     */
    public void createWarningFireworks(Location location) {
        // Create a cluster of fireworks
        for (int i = 0; i < 3; i++) {
            for (int angle = 0; angle < 360; angle += 120) {
                double rad = Math.toRadians(angle);
                double x = Math.cos(rad) * (i+1) * 3;
                double z = Math.sin(rad) * (i+1) * 3;
                
                final Location fireworkLoc = location.clone().add(x, 15 + i*5, z);
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    spawnWarningFirework(fireworkLoc);
                }, i * 3 + angle / 60);
            }
        }
    }
    
    /**
     * Runs the countdown effect for game start
     */
    public void startCountdownEffect(Runnable onComplete) {
        new BukkitRunnable() {
            int countdown = 5;
            
            @Override
            public void run() {
                if (countdown > 0) {
                    // Display countdown
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        player.sendTitle(ChatColor.RED + Integer.toString(countdown), "", 10, 20, 10);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        
                        // Add cool particle effect for countdown
                        player.getWorld().spawnParticle(
                            Particle.REDSTONE, 
                            player.getLocation().add(0, 2, 0), 
                            20, 0.5, 0.5, 0.5, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1)
                        );
                    }
                    countdown--;
                } else {
                    // Start the game
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        player.sendTitle(ChatColor.GREEN + "GO!", "", 10, 20, 10);
                        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
                        
                        // Add particle explosion effect
                        player.getWorld().spawnParticle(
                            Particle.EXPLOSION_LARGE, 
                            player.getLocation().add(0, 1, 0), 
                            3, 0.5, 0.5, 0.5, 0.1
                        );
                    }
                    
                    cancel();
                    onComplete.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
    
    /**
     * Creates visual effects for a player who's been hit
     */
    public void playHitEffects(Player hit) {
        // Small snowball effect
        hit.getWorld().spawnParticle(
            Particle.SNOWBALL, 
            hit.getLocation().add(0, 1, 0), 
            15, 0.3, 0.3, 0.3, 0.05
        );
    }
    
    /**
     * Creates trail effects as a player falls from being hit
     */
    public void createFallingTrail(Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!player.isOnline() || ticks > 20) {
                    cancel();
                    return;
                }
                
                // Only create particles every 3 ticks
                if (ticks % 3 == 0) {
                    player.getWorld().spawnParticle(
                        Particle.CLOUD, 
                        player.getLocation(), 
                        3, 0.1, 0.1, 0.1, 0
                    );
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Creates respawn effects for a player
     */
    public void createRespawnEffect(Player player) {
        player.getWorld().spawnParticle(
            Particle.PORTAL, 
            player.getLocation().add(0, 1, 0), 
            20, 0.5, 0.5, 0.5, 0.1
        );
    }
    
    /**
     * Creates lightning effect at a location (visual only)
     */
    public void spawnFakeLightning(Location location) {
        // Flash effect
        location.getWorld().spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
        
        // Add lightning-like particles
        location.getWorld().spawnParticle(
            Particle.FIREWORKS_SPARK, 
            location.clone().add(0, 1, 0), 
            50, 0.1, 8, 0.1, 0.1
        );
    }
    
    /**
     * Creates victory fireworks for the winner
     */
    public void createVictoryFireworks(Player winner) {
        if (winner == null || !winner.isOnline()) return;
        
        // Victory lightning effect
        spawnFakeLightning(winner.getLocation());
        
        // Multiple fireworks in different patterns
        Location loc = winner.getLocation();
        
        // Circle of fireworks
        final int totalFireworks = 16;
        for (int i = 0; i < totalFireworks; i++) {
            final int fi = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double angle = (fi * 2 * Math.PI) / totalFireworks;
                double radius = 3;
                double x = loc.getX() + radius * Math.cos(angle);
                double z = loc.getZ() + radius * Math.sin(angle);
                
                Location fireworkLoc = new Location(loc.getWorld(), x, loc.getY(), z);
                spawnRandomFirework(fireworkLoc);
            }, i * 2);
        }
        
        // Vertical column of fireworks
        for (int i = 0; i < 5; i++) {
            final int height = i * 3 + 2;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                spawnRandomFirework(loc.clone().add(0, height, 0));
            }, 20 + i * 5);
        }
    }
    
    /**
     * Creates visual effects for a player near win
     */
    public void createNearWinEffects(Player player) {
        // Particle effect
        player.getWorld().spawnParticle(
            Particle.REDSTONE, 
            player.getLocation().add(0, 0.1, 0), 
            20, 0.5, 0.1, 0.5, 0,
            new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0f)
        );
        
        // Add a firework directly above
        spawnWarningFirework(player.getLocation().add(0, 10, 0));
    }
}