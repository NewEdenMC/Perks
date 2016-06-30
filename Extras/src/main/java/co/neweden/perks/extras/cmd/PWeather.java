package co.neweden.perks.extras.cmd;

import co.neweden.perks.extras.Main;
import co.neweden.perks.extras.Util;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PWeather implements CommandExecutor {

    public PWeather() {
        Main.getPlugin().getCommand("pweather").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target = null;
        if (sender instanceof Player)
            target = (Player) sender;
        if (args.length >= 2)
            target = Bukkit.getPlayer(args[0]);

        if (args.length <= 0) {
            if (target != null) {
                getWeather(sender, target);
            }
            usage(sender);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("show")) {
                getWeather(sender, target); return true;
            }
            setWeather(sender, target, args[0]); return true;
        }

        if (args.length == 2) {
            if (!sender.hasPermission("perks.extras.pweather.others")) {
                sender.sendMessage(Util.formatString("&cYou do not have permission to set the player weather for other players")); return true;
            }
            if (target == null) {
                sender.sendMessage(Util.formatString("&c" + args[0] + " is not online")); return true;
            }
            if (args[1].equalsIgnoreCase("show")) {
                getWeather(sender, target); return true;
            }
            setWeather(sender, target, args[1]);
        }

        usage(sender);

        return true;
    }

    private void usage(CommandSender sender) {
        String other = "";
        if (sender.hasPermission("perks.extras.pweather.others"))
            other = "[player] ";
        sender.sendMessage(Util.formatString("&bCommand usage:&f " + other + "show|clear|downfall|off"));
    }

    private void getWeather(CommandSender sender, Player target) {
        if (target == null) {
            sender.sendMessage(Util.formatString("&cCan only get weather for online players")); return;
        }
        String message = "&bPlayer weather for &e" + target.getName() + "&b is ";
        if (target.getPlayerWeather() == null)
            message += "set to the server's weather";
        else
            message += "set to &e" + target.getPlayerWeather().toString();
        sender.sendMessage(Util.formatString(message));
    }

    private void setWeather(CommandSender sender, Player target, String type) {
        if (target == null) {
            sender.sendMessage(Util.formatString("&cWeather can only be set for online player")); return;
        }
        type = type.toLowerCase();
        switch (type) {
            case "clear": target.setPlayerWeather(WeatherType.CLEAR); break;
            case "downfall": target.setPlayerWeather(WeatherType.DOWNFALL); break;
            case "off": target.resetPlayerWeather(); break;
            default: sender.sendMessage(Util.formatString("&cInvalid weather type, see usage below.")); usage(sender); return;
        }
        sender.sendMessage(Util.formatString("&aWeather for &e" + target.getName() + "&a set to &e" + type));
    }

}
