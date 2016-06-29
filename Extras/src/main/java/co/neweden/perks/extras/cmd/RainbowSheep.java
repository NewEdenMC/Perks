package co.neweden.perks.extras.cmd;

import co.neweden.perks.extras.Main;
import co.neweden.perks.extras.Util;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.material.Colorable;

public class RainbowSheep implements CommandExecutor {

    public RainbowSheep() {
        Main.getPlugin().getCommand("rainbowsheep").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Util.formatString("&cYou must specify an online player")); return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Util.formatString("&cThe player you specified is not online")); return true;
        }

        for (DyeColor color : DyeColor.values()) {
            Entity entity = target.getWorld().spawnEntity(target.getLocation(), EntityType.SHEEP);
            ((Colorable) entity).setColor(color);
        }

        sender.sendMessage(Util.formatString("&aSpawned rainbow sheep at " + target.getDisplayName()));

        return true;
    }

}
