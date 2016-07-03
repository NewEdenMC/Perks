package co.neweden.Perks.commands;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;
import co.neweden.Perks.Util;
import co.neweden.Perks.permissions.Permissions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

public class PlayerCommands {

    @SubCommandHelp(
            usage = "player",
            description = "Display information about a perk",
            permission = "perks.admin"
    )
    public static void playerSubCommand(CommandSender sender, String[] args) throws CommandException {
        CommandMain.validatePermission(sender, "perks.admin");
        if (args.length > 1) {
            subCommands(sender, args);
            return;
        }

        HelpPages.renderHelpPage(sender, "player");
    }

    public static void subCommands(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 3)
            throw new UnknownSubCommandException();

        switch (args[2]) {
            case "perms": permissionsCommand(sender, args[1]); break;
            case "balance": balanceCommand(sender, args); break;
            default: throw new UnknownSubCommandException();
        }
    }

    @SubCommandHelp(
            helpPage = "player",
            usage = "player <player-name> perms",
            description = "show permissions for the specified player",
            permission = "perks.admin"
    )
    public static void permissionsCommand(CommandSender sender, String playerName) throws CommandException {
        Map<String, Collection<Perk>> permsMap;
        Player player = Util.getPlayer(playerName);
        if (player == null) {
            OfflinePlayer offlinePlayer = Util.getOfflinePlayer(playerName);
            if (offlinePlayer == null)
                throw new CommandException("The player '" + playerName + "' has either never played before or has changed their name since last login.");
            sender.sendMessage(Util.formatString("&e'" + playerName + "' is offline, therefor active permissions are not available, here are the expected permissions"));
            permsMap = Permissions.getPermsMap(offlinePlayer);
        } else {
            sender.sendMessage(Util.formatString("&bActive permissions for '" + playerName + "'"));
            permsMap = Permissions.getPermsMap(player);
        }
        if (permsMap.isEmpty()) {
            sender.sendMessage(Util.formatString("&eNo permissions found"));
            return;
        }
        for (Map.Entry<String, Collection<Perk>> perm : permsMap.entrySet()) {
            String p = "&f- " + perm.getKey();
            if (perm.getValue().isEmpty()) {
                sender.sendMessage(Util.formatString(p)); continue;
            }
            p += " (";
            for (Perk perk : perm.getValue()) {
                p += perk.getName() + ", ";
            }
            p = p.substring(0, p.length() - 2) + ")";
            sender.sendMessage(Util.formatString(p));
        }
    }

    @SubCommandHelp(
            helpPage = "player",
            usage = "player <player-name> balance",
            description = "get balance for the specified player",
            permission = "perks.admin"
    )
    @SubCommandHelp(
            helpPage = "player",
            usage = "player <player-name> balance set <amount>",
            description = "set balance for the specified player to the amount specified",
            permission = "perks.admin"
    )
    @SubCommandHelp(
            helpPage = "player",
            usage = "player <player-name> balance add <amount>",
            description = "add the specified amount to the specified player's current balance",
            permission = "perks.admin"
    )
    @SubCommandHelp(
            helpPage = "player",
            usage = "player <player-name> balance remove <amount>",
            description = "remove the specified amount from the specified player;s current balance",
            permission = "perks.admin"
    )
    public static void balanceCommand(CommandSender sender, String[] args) throws CommandException {
        OfflinePlayer player = Util.getOfflinePlayer(args[1]);
        if (player == null)
            throw new CommandException("The player '" + args[1] + "' has either never played before or has changed their name since last login.");

        Double bal = Perks.getBalance(player);

        if (args.length == 3) {
            sender.sendMessage(Util.formatString("&aBalance for '" + player.getName() + "' is " + Util.formatCurrency(bal)));
            return;
        }
        if (args.length == 4)
            throw new UnknownSubCommandException("Incorrect syntax for balance sub-command");

        Double updateBal = 0D;
        String updateType = args[3];
        try {
            updateBal = Double.parseDouble(args[4]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            sender.sendMessage(Util.formatString("&cYou did not provide a balance or the balance provided is not a valid number"));
            return;
        }

        if (updateType.equalsIgnoreCase("set")) {
            Perks.setBalance(player, updateBal);
            sender.sendMessage(Util.formatString("&aBalance for '" + player.getName() + "' now set to " + Util.formatCurrency(updateBal)));
        } else if (updateType.equalsIgnoreCase("add")) {
            Perks.setBalance(player, bal + updateBal);
            sender.sendMessage(Util.formatString("&a" + Util.formatCurrency(updateBal) + " added to the balance for '" + player.getName() + "', balance is now " + Util.formatCurrency(bal + updateBal)));
        } else if (updateType.equalsIgnoreCase("remove")) {
            Perks.setBalance(player, bal - updateBal);
            sender.sendMessage(Util.formatString("&a" + Util.formatCurrency(updateBal) + " removed from the balance for '" + player.getName() + "', balance is now " + Util.formatCurrency(bal - updateBal)));
        } else {
            throw new UnknownSubCommandException();
        }

    }

}
