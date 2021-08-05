package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaSave implements CommandExecutor {

    private final ResettableArenas plugin;

    public ArenaSave() {
        this.plugin = (ResettableArenas) Bukkit.getPluginManager().getPlugin(ResettableArenas.NAME);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String arenaName = args[1];

        Arena selectedArena = plugin.ARENA_REGISTRY.getArenaByName(arenaName);
        if (selectedArena == null) {
            sender.sendMessage(ChatColor.RED + "Error: " + arenaName + " is not a valid arena!");
            return false;
        }
        selectedArena.save();
        sender.sendMessage(ChatColor.AQUA + "Commencing save of arena '" + arenaName + "'!");
        return true;
    }
}
