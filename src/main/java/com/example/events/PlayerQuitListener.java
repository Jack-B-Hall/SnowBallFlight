package com.example.events;

import com.example.managers.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final GameManager gameManager;
    
    public PlayerQuitListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Handle player quitting during a game
        gameManager.handlePlayerQuit(event.getPlayer());
    }
}
