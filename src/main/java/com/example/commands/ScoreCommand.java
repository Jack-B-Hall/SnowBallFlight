package com.example.commands;

import com.example.managers.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ScoreCommand implements CommandExecutor {
    private final GameManager gameManager;
    
    public ScoreCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Show scoreboard to all players
        gameManager.showScoreboard();
        return true;
    }
}
