package co.neweden.Perks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Commands implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (sender.hasPermission("perks.admin") && args.length > 0) {
            switch (args[0]) {
                case "help": helpCommand(sender); break;
                case "reload": reloadCommand(sender); break;
                case "balance": balanceCommand(sender, args); break;
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
        if (!sender.hasPermission("perks.admin"))
            sender.sendMessage("You do not have permission to view help information");

        sender.sendMessage(Util.formatString(
                "&bPerks Sub-commands\n" +
                "&f- &ahelp&e: display this help screen\n" +
                "&f- &areload&e: reload the plugin\n" +
                "&f- &abalance&e: commands to manage player balances"
        ));
    }

    private void reloadCommand(CommandSender sender) {
        if (!sender.hasPermission("perks.admin"))
            sender.sendMessage("You do not have permission to perform this command");

        if (Perks.getPlugion().reload())
            sender.sendMessage(Util.formatString("&aReloaded plugin."));
        else
            sender.sendMessage(Util.formatString("&cUnable to reload plugin, check the server console for errors."));
    }

    private void balanceCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("perks.admin")) {
            sender.sendMessage("You do not have permission to perform this command");
            return;
        }

        if (args.length > 1) {
            switch (args[1]) {
                case "get": balanceSubCommand(sender, args); break;
                case "set": balanceSubCommand(sender, args); break;
                case "add": balanceSubCommand(sender, args); break;
                case "remove": balanceSubCommand(sender, args); break;
                default: sender.sendMessage(Util.formatString("&cThe sub-command you ran is not valid, for a list of valied sub-commands run: help."));
            }
            return;
        }

        sender.sendMessage(Util.formatString(
                "&bPerks Balance Sub-commands\n" +
                "&f- &abalance get <player>&e: get balance for the specified player\n" +
                "&f- &abalance set <player> <amount>&e: set balance for the specified player to the amount specified\n" +
                "&f- &abalance add <player> <amount>&e: add the specified amount to the specified player's current balance\n" +
                "&f- &abalance remove <player> <amount>&e: remove the specified amount from the specified player;s current balance"
        ));
    }

    private void balanceSubCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Util.formatString("&cYou did not specify a player."));
            return;
        }

        OfflinePlayer player = Util.getOfflinePlayer(args[2]);
        if (player == null) {
            sender.sendMessage(Util.formatString("&cThe player '" + args[2] + "' has either never played before or has changed their name since last login."));
            return;
        }

        Double bal = Perks.getBalance(player);

        if (args[1].equalsIgnoreCase("get")) {
            sender.sendMessage(Util.formatString("&aBalance for '" + args[2] + "' is " + Util.formatCurrency(bal)));
            return;
        }

        Double updateBal = 0D;
        try {
            updateBal = Double.parseDouble(args[3]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            sender.sendMessage(Util.formatString("&cYou did not provide a balance or the balance provided is not a valid number"));
            return;
        }

        if (args[1].equalsIgnoreCase("set")) {
            Perks.setBalance(player, updateBal);
            sender.sendMessage(Util.formatString("&aBalance for '" + args[2] + "' now set to " + Util.formatCurrency(updateBal)));
        } else if (args[1].equalsIgnoreCase("add")) {
            Perks.setBalance(player, bal + updateBal);
            sender.sendMessage(Util.formatString("&a" + Util.formatCurrency(updateBal) + " added to the balance for '" + args[2] + "', balance is now " + Util.formatCurrency(bal + updateBal)));
        } else if (args[1].equalsIgnoreCase("remove")) {
            Perks.setBalance(player, bal - updateBal);
            sender.sendMessage(Util.formatString("&a" + Util.formatCurrency(updateBal) + " removed from the balance for '" + args[2] + "', balance is now " + Util.formatCurrency(bal - updateBal)));
        }

    }

}
