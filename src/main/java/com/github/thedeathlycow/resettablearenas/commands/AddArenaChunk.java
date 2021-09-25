package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.commands.arguments.ArenaArg;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AddArenaChunk extends SubCommand {

    public AddArenaChunk() {
        super("addChunk", new ArenaArg());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, Argument<?>[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Arena arena = (Arena) args[0].getValue();
            ChunkSnapshot chunk = player.getLocation().getChunk().getChunkSnapshot();
            ArenaChunk arenaChunk = new ArenaChunk(arena, chunk);
            database.addArenaChunk(arenaChunk);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Only players may execute this command.");
            return false;
        }
    }
}
