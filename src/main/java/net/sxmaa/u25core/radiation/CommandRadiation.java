package net.sxmaa.u25core.radiation;

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
        return "/radiation get [player] | level [x y z] | dose <player> <Sv> | contaminate <Sv/h> [x y z] | clear";
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
                EntityRadiationData data = EntityRadiationData.get(player);
                double level = RadiationManager.INSTANCE
                    .getLevel(player.worldObj, player.posX, player.posY, player.posZ);
                sender.addChatMessage(
                    new ChatComponentText(
                        player.getCommandSenderName() + ": level "
                            + RadiationFormat.format(level, "Sv/h")
                            + ", absorbing "
                            + RadiationFormat.format(data.rate, "Sv/h")
                            + ", dose "
                            + RadiationFormat.format(data.dose, "Sv")));
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
                sender.addChatMessage(new ChatComponentText("Level: " + RadiationFormat.format(level, "Sv/h")));
            }
            case "clear" -> {
                EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                ChunkRadiation chunk = RadiationManager.INSTANCE
                    .getChunk(player.worldObj, (int) Math.floor(player.posX) >> 4, (int) Math.floor(player.posZ) >> 4);
                if (chunk != null) {
                    chunk.level = 0;
                    chunk.contamination = 0;
                }
            }
            default -> throw new WrongUsageException(getCommandUsage(sender));
        }
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
