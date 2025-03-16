package com.example.commands;

import com.example.util.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GameStatusCommand implements CommandExecutor {
    private final LocationUtil locationUtil;
    
    public GameStatusCommand(LocationUtil locationUtil) {
        this.locationUtil = locationUtil;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Display header
        sender.sendMessage(ChatColor.GOLD + "=== SnowBallFight Game Status ===");
        
        // Check basic requirements
        int playerCount = sender.getServer().getOnlinePlayers().size();
        if (playerCount < 2) {
            sender.sendMessage(ChatColor.RED + "✗ Players: " + ChatColor.WHITE + 
                              playerCount + "/2 (Need at least 2 players)");
        } else {
            sender.sendMessage(ChatColor.GREEN + "✓ Players: " + ChatColor.WHITE + 
                              playerCount + " online");
        }
        
        // Check spawn points
        List<Location> spawnPoints = locationUtil.getSpawnPoints();
        if (spawnPoints == null || spawnPoints.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "✗ Spawn Points: " + ChatColor.WHITE + "Not set. Use /createSpawn to add spawn points.");
        } else if (spawnPoints.size() < playerCount) {
            sender.sendMessage(ChatColor.RED + "✗ Spawn Points: " + ChatColor.WHITE + 
                              spawnPoints.size() + "/" + playerCount + " (Need at least one per player)");
        } else {
            sender.sendMessage(ChatColor.GREEN + "✓ Spawn Points: " + ChatColor.WHITE + spawnPoints.size() + " points set");
        }
        
        // Check middle point (calculated)
        Location middle = locationUtil.getMiddlePoint();
        
        if (middle == null) {
            sender.sendMessage(ChatColor.RED + "✗ Middle Point: " + ChatColor.WHITE + 
                             "Cannot be calculated. Add spawn points first.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "✓ Middle Point: " + ChatColor.WHITE + 
                             "Automatically calculated at (" + 
                             Math.round(middle.getX()) + ", " + 
                             Math.round(middle.getY()) + ", " + 
                             Math.round(middle.getZ()) + ")");
            
            // Calculate recommended boundary distance if middle point is available
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
            
            // Minimum boundary is furthest spawn point + 10 blocks
            int minimumBoundary = (int) Math.ceil(maxDistance + 10);
            sender.sendMessage(ChatColor.YELLOW + "ℹ Boundary distance: " + ChatColor.WHITE + 
                             minimumBoundary + "+ blocks (based on furthest spawn point)");
        }
        
        // Check loser spot
        if (locationUtil.getLoserSpot() == null) {
            sender.sendMessage(ChatColor.RED + "✗ Loser Spot: " + ChatColor.WHITE + "Not set. Use /setLoserSpot to set the loser area.");
        } else {
            Location loserSpot = locationUtil.getLoserSpot();
            sender.sendMessage(ChatColor.GREEN + "✓ Loser Spot: " + ChatColor.WHITE + 
                              "(" + Math.round(loserSpot.getX()) + ", " + 
                              Math.round(loserSpot.getY()) + ", " + 
                              Math.round(loserSpot.getZ()) + ")");
        }
        
        // Check winner spot
        if (locationUtil.getWinnerSpot() == null) {
            sender.sendMessage(ChatColor.RED + "✗ Winner Spot: " + ChatColor.WHITE + "Not set. Use /setWinnerSpot to set the winner area.");
        } else {
            Location winnerSpot = locationUtil.getWinnerSpot();
            sender.sendMessage(ChatColor.GREEN + "✓ Winner Spot: " + ChatColor.WHITE + 
                              "(" + Math.round(winnerSpot.getX()) + ", " + 
                              Math.round(winnerSpot.getY()) + ", " + 
                              Math.round(winnerSpot.getZ()) + ")");
        }
        
        // Overall status
        boolean spawnPointsSet = spawnPoints != null && !spawnPoints.isEmpty();
        boolean middlePointAvailable = middle != null;
        boolean loserSpotSet = locationUtil.getLoserSpot() != null;
        boolean winnerSpotSet = locationUtil.getWinnerSpot() != null;
        boolean enoughPlayers = playerCount >= 2;
        boolean enoughSpawnPoints = spawnPoints != null && spawnPoints.size() >= playerCount;
        boolean readyToStart = spawnPointsSet && middlePointAvailable && loserSpotSet && winnerSpotSet && enoughPlayers && enoughSpawnPoints;
        
        sender.sendMessage("");
        if (readyToStart) {
            sender.sendMessage(ChatColor.GREEN + "✓ Game is ready to start!");
            sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/startSnowBallFight <hits-to-win>" + 
                              ChatColor.WHITE + " to start a game.");
        } else {
            sender.sendMessage(ChatColor.RED + "✗ Game is not ready yet.");
            sender.sendMessage(ChatColor.WHITE + "Fix the issues above before starting the game.");
        }
        
        sender.sendMessage(ChatColor.GOLD + "===============================");
        return true;
    }
}