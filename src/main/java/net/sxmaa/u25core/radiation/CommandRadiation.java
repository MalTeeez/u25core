package net.sxmaa.u25core.radiation;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

/** /radiation get, level, dose, contaminate and clear. Without coordinates they act at the sender. */
public class CommandRadiation extends CommandBase {

    @Override
    public String getCommandName() {
        return "radiation";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/radiation get [player] | level [x y z] | dose <player> <Sv> | contaminate <Sv/h> [x y z] | clear [chunkRadius]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) throw new WrongUsageException(getCommandUsage(sender));
        switch (args[0]) {
            case "get" -> {
                EntityPlayerMP player = args.length > 1 ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);
                // A one-off geiger counter reading, plus what protection lets through
                ItemRadiation.report(player, sender, true);
                sender.addChatMessage(
                    new ChatComponentText("Absorbing " + RadiationFormat.rate(EntityRadiationData.get(player).rate)));
            }
            case "dose" -> {
                if (args.length < 3) throw new WrongUsageException(getCommandUsage(sender));
                EntityPlayerMP player = getPlayer(sender, args[1]);
                EntityRadiationData.get(player).dose = Math.max(0, parseDouble(sender, args[2]));
            }
            case "contaminate" -> {
                if (args.length < 2) throw new WrongUsageException(getCommandUsage(sender));
                double[] pos = position(sender, args, 2);
                RadiationManager.INSTANCE
                    .contaminate(sender.getEntityWorld(), pos[0], pos[1], pos[2], parseDouble(sender, args[1]));
            }
            case "level" -> {
                double[] pos = position(sender, args, 1);
                double level = RadiationManager.INSTANCE.getLevel(sender.getEntityWorld(), pos[0], pos[1], pos[2]);
                sender.addChatMessage(new ChatComponentText("Level: " + RadiationFormat.rate(level)));
            }
            case "clear" -> {
                // Neighbours spread back into a lone cleared chunk, so clear a square around the sender
                EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                int r = args.length > 1 ? parseIntBounded(sender, args[1], 0, 32) : 2;
                int cx = (int) Math.floor(player.posX) >> 4, cz = (int) Math.floor(player.posZ) >> 4;
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        ChunkRadiation chunk = RadiationManager.INSTANCE.getChunk(player.worldObj, cx + dx, cz + dz);
                        if (chunk != null) {
                            chunk.level = 0;
                            chunk.contamination = 0;
                        }
                    }
                }
                sender.addChatMessage(
                    new ChatComponentText("Cleared radiation within " + r + " chunks; active sources rebuild it."));
            }
            default -> throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "get", "level", "dose", "contaminate", "clear");
        if (args.length == 2 && (args[0].equals("get") || args[0].equals("dose"))) {
            return getListOfStringsMatchingLastWord(
                args,
                net.minecraft.server.MinecraftServer.getServer()
                    .getAllUsernames());
        }
        return null;
    }

    /** x y z from the arguments at {@code from}, or the sender's position if they're missing. */
    private static double[] position(ICommandSender sender, String[] args, int from) {
        if (args.length >= from + 3) {
            return new double[] { parseDouble(sender, args[from]), parseDouble(sender, args[from + 1]),
                parseDouble(sender, args[from + 2]) };
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        return new double[] { player.posX, player.posY, player.posZ };
    }
}
