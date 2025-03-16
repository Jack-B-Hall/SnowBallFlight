package com.example.commands;

import com.example.util.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ResetGameCommand implements CommandExecutor {
    private final LocationUtil locationUtil;
    
    public ResetGameCommand(LocationUtil locationUtil) {
        this.locationUtil = locationUtil;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reset all location settings
        boolean success = locationUtil.resetAllLocations();
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "All game settings have been reset!");
            sender.sendMessage(ChatColor.YELLOW + "You'll need to set up the game again:");
            sender.sendMessage(ChatColor.WHITE + "1. Create spawn points with " + ChatColor.YELLOW + "/createSpawn");
            sender.sendMessage(ChatColor.WHITE + "2. Set the middle point with " + ChatColor.YELLOW + "/setMiddle");
            sender.sendMessage(ChatColor.WHITE + "3. Set the loser spot with " + ChatColor.YELLOW + "/setLoserSpot");
            sender.sendMessage(ChatColor.WHITE + "4. Set the winner spot with " + ChatColor.YELLOW + "/setWinnerSpot");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to reset game settings.");
        }
        
        return true;
    }
}
