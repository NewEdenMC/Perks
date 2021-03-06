package co.neweden.perks.extras.cmd;

import co.neweden.perks.extras.Main;
import co.neweden.perks.extras.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HighFive implements CommandExecutor {

    public HighFive() {
        Main.getPlugin().getCommand("highfive").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Util.formatString("&cYou must specify an online player")); return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Util.formatString("&cThe player you specified is not online")); return true;
        }

        String senderName = sender.getName();
        if (sender instanceof Player)
            senderName = ((Player) sender).getDisplayName();

        new PotionEffect(PotionEffectType.HEAL, 1, 1).apply(target);
        Bukkit.getServer().broadcastMessage(Util.formatString("&c" + senderName + "&7 high fived &c" + target.getDisplayName()));

        return true;
    }

}
