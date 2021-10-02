package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.commands.arguments.*;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddArenaChunk extends SubCommand {


    public AddArenaChunk() {
        super("addChunk", new ArenaArg(), new StringArg(), new IntegerArg(), new IntegerArg());
    }

    @Override
    public boolean isValidArgs(List<String> argsStr) {

        if (!(argsStr.size() == 1 || argsStr.size() == 4)) {
            System.out.println("Invalid length");
            return false;
        }

        for (int i = 0; i < argsStr.size(); i++) {
            Argument<?> arg = this.getArgs().get(i);
            try {
                arg.parseArg(argsStr.get(i));
            } catch (ArgParseException e) {
                System.out.println("Error parsing argument: " + e);
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, Argument<?>[] args, int numArgs) {
        ChunkSnapshot chunk = null;
        Arena arena = (Arena) args[0].getValue();

        switch (numArgs) {
            case 1:
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    chunk = player.getLocation().getChunk().getChunkSnapshot();
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players may execute this command.");
                    return false;
                }
                break;
            case 4:
                String worldName = (String) args[1].getValue();
                int blockX = 16 * (Integer) args[2].getValue();
                int blockZ = 16 * (Integer) args[3].getValue();

                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "Error: The specified world does not exist!");
                    return false;
                }

                Location loc = new Location(world, blockX, 0, blockZ);
                chunk = world.getChunkAt(loc).getChunkSnapshot();
                break;
        }

        ArenaChunk arenaChunk = new ArenaChunk(arena, chunk);
        arenaChunk.setSaveVersion(arena.getSaveVersion());
        arenaChunk.setLoadVersion(arena.getLoadVersion());
        database.addArenaChunk(arenaChunk);
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        sender.sendMessage(ChatColor.GREEN + "Successfully added chunk " + chunkX + ", " + chunkZ + " to arena " + arena.getName() + "!");
        return true;
    }
}
