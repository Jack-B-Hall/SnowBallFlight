package com.example.commands;

import com.example.managers.GameManager;
import com.example.util.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class StartGameCommand implements CommandExecutor {
    private final GameManager gameManager;
    private final LocationUtil locationUtil;
    
    public StartGameCommand(GameManager gameManager, LocationUtil locationUtil) {
        this.gameManager = gameManager;
        this.locationUtil = locationUtil;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Run validation checks first
        if (!validateGameStart(sender)) {
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /startSnowBallFight <hits-to-win>");
            return false;
        }
        
        int hitsToWin;
        
        try {
            hitsToWin = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number! Usage: /startSnowBallFight <hits-to-win>");
            return false;
        }
        
        if (hitsToWin <= 0) {
            sender.sendMessage(ChatColor.RED + "Hits-to-win must be a positive number!");
            return false;
        }
        
        // Ensure middle point is calculated
        Location middle = locationUtil.getMiddlePoint();
        if (middle == null) {
            sender.sendMessage(ChatColor.RED + "Cannot calculate middle point. Create spawn points first!");
            return true;
        }
        
        // Calculate boundary distance automatically
        List<Location> spawnPoints = locationUtil.getSpawnPoints();
        
        double maxDistance = 0;
        for (Location spawn : spawnPoints) {
            // Only consider X and Z for the distance calculation
            double dx = spawn.getX() - middle.getX();
            double dz = spawn.getZ() - middle.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        
        // Set boundary to furthest spawn point + 20 blocks for safety
        double boundaryDistance = maxDistance + 20;
        
        // Display information about middle point
        sender.sendMessage(ChatColor.YELLOW + "Using automatically calculated middle point at " + 
                     ChatColor.WHITE + "(" + Math.round(middle.getX()) + ", " + 
                     Math.round(middle.getY()) + ", " + 
                     Math.round(middle.getZ()) + ")");
        
        // All validation passed, start the game
        gameManager.startGame(hitsToWin, boundaryDistance);
        
        // Announce the game start
        sender.sendMessage(ChatColor.GREEN + "SnowBall Fight has started! " + 
                          ChatColor.YELLOW + "First to " + hitsToWin + " hits wins!");
        
        return true;
    }
    
    /**
     * Validates if all requirements are met to start the game
     * @param sender The command sender
     * @return true if validation passes, false otherwise
     */
    private boolean validateGameStart(CommandSender sender) {
        // Check if a game is already active
        if (gameManager.isGameActive()) {
            sender.sendMessage(ChatColor.RED + "A game is already in progress! Use /endGame to end it.");
            return false;
        }
        
        // Check if winner spot is set
        if (locationUtil.getWinnerSpot() == null) {
            sender.sendMessage(ChatColor.RED + "Winner spot not set! Use /setWinnerSpot to set it.");
            return false;
        }
        
        // Check if loser spot is set
        if (locationUtil.getLoserSpot() == null) {
            sender.sendMessage(ChatColor.RED + "Loser spot not set! Use /setLoserSpot to set it.");
            return false;
        }
        
        // Check if spawn points are set
        List<Location> spawnPoints = locationUtil.getSpawnPoints();
        if (spawnPoints == null || spawnPoints.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No spawn points set! Use /createSpawn to create spawn points.");
            return false;
        }
        
        // Check if there are enough players
        int playerCount = sender.getServer().getOnlinePlayers().size();
        if (playerCount < 2) {
            sender.sendMessage(ChatColor.RED + "Not enough players! Need at least 2 players to start.");
            return false;
        }
        
        // Check if there are enough spawn points for all players
        if (spawnPoints.size() < playerCount) {
            sender.sendMessage(ChatColor.RED + "Not enough spawn points! Need at least " + playerCount + 
                             " spawn points for all players. Currently have " + spawnPoints.size() + ".");
            return false;
        }
        
        return true;
    }
}