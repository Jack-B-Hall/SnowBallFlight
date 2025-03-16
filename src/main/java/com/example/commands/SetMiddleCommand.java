package com.example.commands;

import com.example.util.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMiddleCommand implements CommandExecutor {
    private final LocationUtil locationUtil;
    
    public SetMiddleCommand(LocationUtil locationUtil) {
        this.locationUtil = locationUtil;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Set the middle point to the player's current location
        locationUtil.setMiddlePoint(player.getLocation());
        
        player.sendMessage(ChatColor.GREEN + "Middle point set at your current location!");
        return true;
    }
}
