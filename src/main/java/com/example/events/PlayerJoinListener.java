package com.example.events;

import com.example.managers.GameManager;
import com.example.managers.PlayerStatusManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final GameManager gameManager;
    private final PlayerStatusManager playerStatusManager;
    
    public PlayerJoinListener(GameManager gameManager, PlayerStatusManager playerStatusManager) {
        this.gameManager = gameManager;
        this.playerStatusManager = playerStatusManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Immediately update player status (adventure mode, health, food)
        playerStatusManager.updatePlayerStatus(event.getPlayer());
        
        // Handle player joining during a game
        gameManager.handlePlayerJoin(event.getPlayer());
    }
}