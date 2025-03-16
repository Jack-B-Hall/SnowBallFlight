package com.example.events;

import com.example.managers.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener implements Listener {
    private final GameManager gameManager;
    
    public ProjectileLaunchListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        // Handle snowball launch events
        gameManager.handleSnowballThrow(event);
    }
}
