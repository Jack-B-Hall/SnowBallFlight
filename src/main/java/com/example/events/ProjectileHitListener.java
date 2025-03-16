package com.example.events;

import com.example.managers.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener implements Listener {
    private final GameManager gameManager;
    
    public ProjectileHitListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // Check if it's a snowball and a game is active
        if (gameManager.isGameActive() && event.getEntity() instanceof Snowball) {
            // Check if the snowball hit a player
            if (event.getHitEntity() instanceof Player) {
                // Check if the snowball was thrown by a player
                if (event.getEntity().getShooter() instanceof Player) {
                    Player thrower = (Player) event.getEntity().getShooter();
                    Player hit = (Player) event.getHitEntity();
                    
                    // Handle the hit
                    gameManager.handleSnowballHit(thrower, hit);
                }
            }
        }
    }
}