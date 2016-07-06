package co.neweden.perks.extras.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import co.neweden.perks.extras.Cooldown;
import co.neweden.perks.extras.Main;
import co.neweden.perks.extras.Util;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Pickup implements CommandExecutor {

    private Integer defaultradius = 15; // TODO: make configurable
    private Integer maxradius = 20; // TODO: make configurable
    private Integer cooldownTime = 10; // TODO: make configurable
    Cooldown cooldown = new Cooldown(10);

    public Pickup() {
        Main.getPlugin().getCommand("pickup").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Util.formatString("&cYou must be a player to run that command.")); return true;
        }

        long cooldownEnd = cooldown.getCooldown(sender);
        if (cooldownEnd > 0) {
            sender.sendMessage(Util.formatString("&cYou cannot run this command so soon after you last ran it, you must wait " + cooldownEnd + " seconds")); return true;
        }

        Player player = (Player) sender;

        int radius = defaultradius;
        if (args.length > 0) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(Util.formatString("&cYou must supply an integer"));
                return true;
            }
            if (radius > maxradius && !player.hasPermission("perks.extras.pickup.bypass")) {
                player.sendMessage(Util.formatString("&cYou are not allowed to pickup more than the max radius of " + maxradius + "."));
                return true;
            }
        }

        boolean added = false;
        boolean dropped = false;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Item) {
                ItemStack itemstack = ((Item) entity).getItemStack();
                entity.remove();
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemstack);
                if (!leftover.isEmpty()) {
                    for (Entry<Integer, ItemStack> todrop : leftover.entrySet()) {
                        player.getWorld().dropItem(player.getLocation(), todrop.getValue());
                    }
                    dropped = true;
                } else
                    added = true;
            }
        }

        String message;
        if (added)
            message = "You just picked up some items";
        else
            message = "You did not pickup any items";

        if (dropped)
            message += " however some items were dropped at your feet as your inventory is full";

        message += ".";

        cooldown.setCooldown(sender);
        sender.sendMessage(Util.formatString("&a" + message));
        return true;
    }

}
