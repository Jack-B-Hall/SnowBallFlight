package com.example.commands;

import com.example.util.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateSpawnCommand implements CommandExecutor {
    private final LocationUtil locationUtil;
    
    public CreateSpawnCommand(LocationUtil locationUtil) {
        this.locationUtil = locationUtil;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Add the player's current location as a spawn point
        locationUtil.addSpawnPoint(player.getLocation());
        
        player.sendMessage(ChatColor.GREEN + "Spawn point created at your current location!");
        return true;
    }
}
