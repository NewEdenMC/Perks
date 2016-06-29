package co.neweden.perks.extras.cmd;

import co.neweden.perks.extras.Main;
import co.neweden.perks.extras.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.ArrayList;
import java.util.List;

public class Prefix implements CommandExecutor {

    public Prefix() {
        Main.getPlugin().getCommand("prefix").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Util.formatString("&cOnly players can run this command")); return true;
        }

        PermissionUser user = PermissionsEx.getUser((Player) sender);

        if (args.length < 1) {
            sender.sendMessage(Util.formatString("&cYou must specify the prefix to set")); return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            user.setPrefix(null, null);
            sender.sendMessage(Util.formatString("&bYou have removed your prefix."));
            return true;
        }

        String prefix = "";
        for (int i = 0; i <= args.length - 1; i++) {
            prefix = prefix + args[i];
            if (i != (args.length - 1))
                prefix = prefix + " ";
        }

        String prev_prefix = user.getPrefix();
        user.setPrefix(prefix, null);

        if (prev_prefix.equals("")) {
            sender.sendMessage(Util.formatString("&bYou have changed your prefix to " + prefix + "&b."));
        } else {
            sender.sendMessage(Util.formatString("&bYou have changed your prefix from " + prev_prefix + "&b to " + prefix + "&b."));
        }

        return true;

    }

}
