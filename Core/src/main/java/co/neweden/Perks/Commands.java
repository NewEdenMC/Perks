package co.neweden.Perks;

import org.bukkit.OfflinePlayer;
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
                case "balance": balanceCommand(sender, args); break;
                case "realms": realmsCommand(sender); break;
                case "buy": buyCommand(sender, args); break;
                default: defaultCommand(sender, args);
            }
            return true;
        }

        if (sender instanceof Player) {
            Perks.getCurrentRealm().getPerksMenu().openMenu((Player) sender);
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
                "&f- &abalance&e: commands to manage player balances\n" +
                "&f- &arealms&e: display list of realms\n" +
                "&f- &abuy <perk>&e: purchase the specified perk\n" +
                "&f- &a<realm-name>&e: to see perks for a specific realm just enter the name of the realm as the sub-command"
        ));
    }

    private void reloadCommand(CommandSender sender) {
        if (!sender.hasPermission("perks.admin")) {
            sender.sendMessage("You do not have permission to perform this command");
            return;
        }

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

    private void realmsCommand(CommandSender sender) {
        if (sender instanceof Player) {
            Perks.getRealmsMenu().openMenu((Player) sender);
            return;
        }

        sender.sendMessage(Util.formatString("&bList of realms"));
        for (Realm realm : Perks.getRealms()) {
            sender.sendMessage(Util.formatString("&f- &a" + realm.getName() + "&e: " + realm.getDisplayName()));
        }
    }

    private void buyCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Util.formatString("&cOnly players can run this command."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Util.formatString("&cYou did not specify a perk."));
            return;
        }

        Perk perk = null;
        for (Perk p : Perks.getPerks()) {
            if (p.getName().equalsIgnoreCase(args[1]))
                perk = p;
        }
        if (perk == null) {
            sender.sendMessage(Util.formatString("&cThe perk you are trying to purchase is not recognised."));
            return;
        }

        Player player = (Player) sender;
        Perk.PurchaseStatus ps = perk.purchaseStatus(player);
        if (ps.equals(Perk.PurchaseStatus.OWNS_PERK) || ps.equals(Perk.PurchaseStatus.HAS_ALL_PERMISSIONS)) {
            player.sendMessage(Util.formatString("&cYou currently already own this perk or this perk has been automatically activated based on your current permissions."));
            return;
        }
        if (ps.equals(Perk.PurchaseStatus.INSUFFICIENT_FUNDS)) {
            player.sendMessage(Util.formatString("&cYou do not have enough credit to buy this perk"));
            return;
        }
        if (!perk.getMemberRealms().contains(Perks.getCurrentRealm())) {
            player.sendMessage(Util.formatString("&eWarning you won't be able to use the perk '" + perk.getDisplayName() + "' in '" + Perks.getCurrentRealm().getDisplayName() + "' which is the realm you are in currently"));
        }

        if (perk.purchasePerk(player))
            player.sendMessage(Util.formatString("&aYou have just purchased " + perk.getDisplayName()));
        else
            player.sendMessage(Util.formatString("&cAn unknown error occurred while trying to purchase this perk, please contact a member of staff."));
    }

    private void defaultCommand(CommandSender sender, String[] args) {
        if (args.length < 1) helpCommand(sender);
        Realm realm = null;
        for (Realm r : Perks.getRealms()) {
            if (r.getName().equalsIgnoreCase(args[0])) realm = r;
        }
        if (realm == null) {
            sender.sendMessage(Util.formatString("&cThe sub-command or realm name you entered is not valid, for a list of valied sub-commands run: help."));
            return;
        }

        if (sender instanceof Player) {
            realm.getPerksMenu().openMenu((Player) sender);
            return;
        }

        sender.sendMessage(Util.formatString("&b" + realm.getDisplayName() + " Perks"));
        for (Perk perk : Perks.getPerks()) {
            if (!perk.getMemberRealms().contains(realm)) continue;
            sender.sendMessage(Util.formatString("&f- &a" + perk.getName() + "&e: " + perk.getDisplayName()));
        }
    }

}
