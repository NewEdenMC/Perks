package co.neweden.perks.extras.cmd;

import co.neweden.perks.extras.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Prefix {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("prefix")) {

            if (sender instanceof Player) {
                Player player = (Player) sender;
                PermissionUser user = PermissionsEx.getUser(player);

                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("off")) {
                        user.setPrefix(null, null);
                        sender.sendMessage(ChatColor.AQUA + "You have removed your prefix.");
                    } else {
                        String prefix = "";
                        for (int i = 0; i <= (args.length - 1); i++) {
                            prefix = prefix + args[i];
                            if (i != (args.length - 1)) {
                                prefix = prefix + " ";
                            }
                        }

                        String prev_prefix = user.getPrefix();
                        user.setPrefix(prefix, null);

                        if (prev_prefix.equals("")) {
                            sender.sendMessage(Util.formatString("&bYou have changed your prefix to " + prefix + "&b."));
                        } else {
                            sender.sendMessage(Util.formatString("&bYou have changed your prefix from " + prev_prefix + "&b to " + prefix + "&b."));
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.AQUA + "You must specify a prefix.");
                    //sender.sendMessage(ChatColor.AQUA + "If you would like to remove your prefix type " + ChatColor.YELLOW + "/prefix off" + ChatColor.AQUA);
                }
            } else {
                sender.sendMessage(ChatColor.AQUA + "You must be a player to run this command.");
            }

            return true;
        }

        return false;

    }

}
