package com.example;

import com.example.commands.*;
import com.example.events.*;
import com.example.managers.GameManager;
import com.example.managers.PlayerStatusManager;
import com.example.util.LocationUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowBallFight extends JavaPlugin {
    private GameManager gameManager;
    private LocationUtil locationUtil;
    private PlayerStatusManager playerStatusManager;
    
    @Override
    public void onEnable() {
        // Create plugin data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        
        // Initialize location utility with data folder
        locationUtil = new LocationUtil(this);
        
        // Initialize player status manager
        playerStatusManager = new PlayerStatusManager(this);
        
        // Initialize game manager
        gameManager = new GameManager(this, locationUtil, playerStatusManager);
        
        // Register commands
        registerCommands();
        
        // Register event listeners
        registerEventListeners();
        
        getLogger().info("SnowBallFight plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // End any active game
        if (gameManager.isGameActive()) {
            gameManager.endGame();
        }
        
        getLogger().info("SnowBallFight plugin has been disabled!");
    }
    
    private void registerCommands() {
        // Register setup commands
        getCommand("createSpawn").setExecutor(new CreateSpawnCommand(locationUtil));
        getCommand("setLoserSpot").setExecutor(new SetLoserSpotCommand(locationUtil));
        getCommand("setWinnerSpot").setExecutor(new SetWinnerSpotCommand(locationUtil));
        
        // Register management commands
        getCommand("startSnowBallFight").setExecutor(new StartGameCommand(gameManager, locationUtil));
        getCommand("endGame").setExecutor(new EndGameCommand(gameManager));
        getCommand("score").setExecutor(new ScoreCommand(gameManager));
        
        // Register new commands
        getCommand("gameStatus").setExecutor(new GameStatusCommand(locationUtil));
        getCommand("resetGame").setExecutor(new ResetGameCommand(locationUtil));
    }
    
    private void registerEventListeners() {
        // Register all event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(gameManager, playerStatusManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(gameManager), this);
    }
}