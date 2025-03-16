package com.example.events;

import com.example.managers.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final GameManager gameManager;
    
    public PlayerMoveListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only handle movement if a game is active
        if (gameManager.isGameActive()) {
            // Check if the player moved (change in x, y, or z)
            if (event.getTo().getX() != event.getFrom().getX() || 
                event.getTo().getY() != event.getFrom().getY() || 
                event.getTo().getZ() != event.getFrom().getZ()) {
                
                gameManager.handlePlayerMove(event.getPlayer(), event.getTo());
            }
        }
    }
}
