package net.sxmaa.u25core.dump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

/** /u25core dump [all|recipes|nei], client side. */
public class CommandDump extends CommandBase {

    @Override
    public String getCommandName() {
        return "u25core";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/u25core dump [all|" + String.join("|", DataDump.PARTS) + "]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1 || !"dump".equals(args[0])) throw new WrongUsageException(getCommandUsage(sender));
        List<String> parts = parts(args.length > 1 ? args[1] : "all");
        if (parts == null) throw new WrongUsageException(getCommandUsage(sender));
        if (!DataDump.ready()) {
            sender.addChatMessage(new ChatComponentText("NEI hasn't finished loading yet"));
            return;
        }
        if (DataDump.runAsync(parts, summary -> sender.addChatMessage(new ChatComponentText("Dumped " + summary)))) {
            sender.addChatMessage(new ChatComponentText("Dumping " + parts + " in the background"));
        } else {
            sender.addChatMessage(new ChatComponentText("A dump is already running"));
        }
    }

    /** Parts for all or one part name, null when unknown. */
    static List<String> parts(String arg) {
        if ("all".equals(arg)) return DataDump.PARTS;
        return DataDump.PARTS.contains(arg) ? Collections.singletonList(arg) : null;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "dump");
        if (args.length == 2) {
            List<String> options = new ArrayList<>(DataDump.PARTS);
            options.add("all");
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        return null;
    }
}
