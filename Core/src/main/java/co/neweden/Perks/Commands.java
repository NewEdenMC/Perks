package co.neweden.Perks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "help": helpCommand(sender); break;
                case "reload": reloadCommand(sender); break;
                default: sender.sendMessage(Util.formatString("&cThe sub-command you ran is not valid, for a list of valied sub-commands run: help."));
            }
            return true;
        }

        if (sender instanceof Player) {
            Perks.openPerksMenu((Player) sender);
            return true;
        }

        helpCommand(sender);

        return true;
    }

    private void helpCommand(CommandSender sender) {
        sender.sendMessage(Util.formatString(
                "&bPerks Sub-commands\n" +
                "&f- &ahelp&e: display this help screen\n" +
                "&f- &areload&e: reload the plugin"
        ));
    }

    private void reloadCommand(CommandSender sender) {
        if (Perks.getPlugion().reload())
            sender.sendMessage(Util.formatString("&aReloaded plugin."));
        else
            sender.sendMessage(Util.formatString("&cUnable to reload plugin, check the server console for errors."));
    }

}
