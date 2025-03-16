package com.example.commands;

import com.example.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndGameCommand implements CommandExecutor {
    private final GameManager gameManager;
    
    public EndGameCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!gameManager.isGameActive()) {
            sender.sendMessage(ChatColor.RED + "No game is currently active!");
            return true;
        }
        
        // End the game
        gameManager.endGame();
        
        sender.sendMessage(ChatColor.GREEN + "Game ended!");
        return true;
    }
}