package co.neweden.Perks.commands;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;
import co.neweden.Perks.Util;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PerkCommands {

    @SubCommandHelp(
            usage = "perk",
            description = "Display information about a perk",
            permission = "perks.perks"
    )
    public static void perkSubCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            subCommands(sender, args);
            return;
        }

        HelpPages.renderHelpPage(sender, "perk");
    }

    public static void subCommands(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 3)
            throw new UnknownSubCommandException();

        Perk perk = null;
        for (Perk p : Perks.getPerks()) {
            if (p.getName().equalsIgnoreCase(args[1]))
                perk = p;
        }
        if (perk == null)
            throw new CommandException("The perk you specified is not recognised.");

        switch (args[2]) {
            case "buy": buyCommand(sender, perk); break;
            case "perms": permissionsCommand(sender, perk); break;
            default: throw new UnknownSubCommandException();
        }
    }

    @SubCommandHelp(
            helpPage = "perk",
            usage = "perk <perk-name> buy",
            description = "buy the specified perk",
            permission = "perks.perks"
    )
    public static void buyCommand(CommandSender sender, Perk perk) throws CommandException {
        CommandMain.validatePlayer(sender);
        Player player = (Player) sender;
        Perk.PurchaseStatus ps = perk.purchaseStatus(player);

        if (ps.equals(Perk.PurchaseStatus.OWNS_PERK) || ps.equals(Perk.PurchaseStatus.HAS_ALL_PERMISSIONS))
            throw new CommandException("You currently already own this perk or this perk has been automatically activated based on your current permissions.");
        if (ps.equals(Perk.PurchaseStatus.INSUFFICIENT_FUNDS))
            throw new CommandException("You do not have enough credit to buy this perk");
        if (!perk.getMemberRealms().contains(Perks.getCurrentRealm())) {
            player.sendMessage(Util.formatString("&eWarning you won't be able to use the perk '" + perk.getDisplayName() + "' in '" + Perks.getCurrentRealm().getDisplayName() + "' which is the realm you are in currently"));
        }

        if (perk.purchasePerk(player))
            player.sendMessage(Util.formatString("&aYou have just purchased " + perk.getDisplayName()));
        else
            player.sendMessage(Util.formatString("&cAn unknown error occurred while trying to purchase this perk, please contact a member of staff."));
    }

    @SubCommandHelp(
            helpPage = "perk",
            usage = "perk <perk-name> perms",
            description = "show permissions for the specified perk",
            permission = "perks.admin"
    )
    public static void permissionsCommand(CommandSender sender, Perk perk) throws CommandException {
        CommandMain.validatePermission(sender, "perks.admin");
        sender.sendMessage(Util.formatString("&bPermissions for '" + perk.getName() + "'"));
        for (String perm : perk.getPermissions()) {
            sender.sendMessage(Util.formatString("&f- " + perm));
        }
    }

}
