package co.neweden.perks.extras.cmd;

import co.neweden.perks.extras.Cooldown;
import co.neweden.perks.extras.Main;
import co.neweden.perks.extras.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Slap implements CommandExecutor {

    Cooldown cooldown = new Cooldown(10);

    public Slap() {
        Main.getPlugin().getCommand("slap").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Util.formatString("&cYou must specify an online player")); return true;
        }

        long cooldownEnd = cooldown.getCooldown(sender);
        if (cooldownEnd > 0) {
            sender.sendMessage(Util.formatString("&cYou cannot run this command so soon after you last ran it, you must wait " + cooldownEnd + " seconds")); return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Util.formatString("&cThe player you specified is not online")); return true;
        }

        if (target.hasPermission("perks.extras.canttouchthis")) {
            sender.sendMessage(Util.formatString("&cYou cannnot bitch slap " + target.getName())); return true;
        }

        String senderName = sender.getName();
        if (sender instanceof Player)
            senderName = ((Player) sender).getDisplayName();

        new PotionEffect(PotionEffectType.POISON, 1, 100).apply(target);
        cooldown.setCooldown(sender);
        Bukkit.getServer().broadcastMessage(Util.formatString("&c" + target.getDisplayName() + "&7 has been slapped by &c" + senderName));

        return true;
    }

}
