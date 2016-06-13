package co.neweden.Perks.commands;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;
import co.neweden.Perks.Realm;
import co.neweden.Perks.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMain implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length > 0) {
            subCommand(sender, args);
            return true;
        }

        if (sender instanceof Player) {
            Perks.getCurrentRealm().getPerksMenu().openMenu((Player) sender);
            return true;
        }

        HelpPages.renderHelpPage(sender);

        return true;
    }

    static void validatePermission(CommandSender sender, String node) throws InsufficientPermissionsException {
        if (!sender.hasPermission(node)) throw new InsufficientPermissionsException();
    }

    static void validatePermission(CommandSender sender, String node, String message) throws InsufficientPermissionsException {
        if (!sender.hasPermission(node)) throw new InsufficientPermissionsException(message);
    }

    static void validatePlayer(CommandSender sender) throws CommandException {
        validatePlayer(sender, "Only players can run this command");
    }

    static void validatePlayer(CommandSender sender, String message) throws CommandException {
        if (!(sender instanceof Player)) throw new CommandException(message);
    }

    @SubCommandHelp(
            usage = "help",
            description = "Display this help screen",
            permission = "perks.perks"
    )
    public static void subCommand(CommandSender sender, String[] args) {
        try {
            switch (args[0]) {
                case "help": HelpPages.renderHelpPage(sender); break;
                case "reload": reloadCommand(sender); break;
                case "realms": realmsCommand(sender); break;
                case "perk": PerkCommands.perkSubCommand(sender, args); break;
                case "player": PlayerCommands.playerSubCommand(sender, args); break;
                default: defaultCommand(sender, args);
            }
        } catch (CommandException e) {
            sender.sendMessage(Util.formatString("&c" + e.getMessage()));
        }
    }

    @SubCommandHelp(
            usage = "reload",
            description = "Reload the plugin",
            permission = "perks.admin"
    )
    public static void reloadCommand(CommandSender sender) throws CommandException {
        validatePermission(sender, "perks.admin");

        if (Perks.getPlugion().reload())
            sender.sendMessage(Util.formatString("&aReloaded plugin."));
        else
            sender.sendMessage(Util.formatString("&cUnable to reload plugin, check the server console for errors."));
    }

    @SubCommandHelp(
            usage = "realms",
            description = "Display realms",
            permission = "perks.perks"
    )
    public static void realmsCommand(CommandSender sender) throws CommandException {
        validatePermission(sender, "perks.perks");

        sender.sendMessage(Util.formatString("&bList of realms"));
        for (Realm realm : Perks.getRealms()) {
            sender.sendMessage(Util.formatString("&f- " + realm.getName() + ": &b" + realm.getDisplayName()));
        }
    }

    @SubCommandHelp(
            usage = "<realm-name>",
            description = "Display perks for a specific realm",
            permission = "perks.perks"
    )
    public static void defaultCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) HelpPages.renderHelpPage(sender);
        Realm realm = null;
        for (Realm r : Perks.getRealms()) {
            if (r.getName().equalsIgnoreCase(args[0])) realm = r;
        }
        if (realm == null) {
            throw new UnknownSubCommandException("The sub-command or realm name you entered is not valid, for a list of valid sub-commands run: help.");
        }

        if (sender instanceof Player) {
            realm.getPerksMenu().openMenu((Player) sender);
            return;
        }

        sender.sendMessage(Util.formatString("&b" + realm.getDisplayName() + " Perks"));
        for (Perk perk : Perks.getPerks()) {
            if (!perk.getMemberRealms().contains(realm)) continue;
            sender.sendMessage(Util.formatString("&f- " + perk.getName() + ": &b" + perk.getDisplayName()));
        }
    }

}
