package co.neweden.perks.extras.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import co.neweden.perks.extras.Main;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class Pickup implements Listener {

    private Integer defaultradius;
    private Integer maxradius;
    private Map<Player, Long> Cooldown = new HashMap<Player, Long>();

    public Pickup() {
        defaultradius = Main.getPlugin().getConfig().getInt("DefaultRadius");
        maxradius = Main.getPlugin().getConfig().getInt("MaximumRadius");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            pickup((Player) sender, args);
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to run that command.");
        }
        return true;
    }

    public boolean pickup(Player player, String[] args) {
        Integer radius = defaultradius;
        Integer added = 0;
        Integer notadded = 0;

        if (!player.hasPermission("ipickup.bypass.cooldown")) {
            if (Cooldown != null) {
                if (Cooldown.containsKey(player)) {
                    Long timeremaining = System.currentTimeMillis() - Cooldown.get(player);
                    Long timeremainingsec = timeremaining/1000;
                    Integer cooldownsec = Main.getPlugin().getConfig().getInt("Cooldown");
                    Integer cooldownmilisec = cooldownsec * 1000;
                    if (timeremaining > cooldownmilisec) {
                        Cooldown.remove(player);
                    } else {
                        player.sendMessage(ChatColor.AQUA + "You need to wait another " + ChatColor.GREEN + (cooldownsec - timeremainingsec) + ChatColor.AQUA + " seconds before you can run that command.");
                        return false;
                    }
                }
            }
        }

        if (args.length != 0) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (Exception e) {
                player.sendMessage(ChatColor.AQUA + "You must supply an integer");
                return false;
            }
            if (radius > maxradius && !player.hasPermission("ipickup.bypass.radius")) {
                player.sendMessage(ChatColor.AQUA + "You are not allowed to pickup more than the max radius of " + maxradius + ".");
                return false;
            }
        }

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Item) {
                ItemStack itemstack = ((Item) entity).getItemStack();
                (entity).remove();
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemstack);
                if (!leftover.isEmpty()) {
                    for (Entry<Integer, ItemStack> entitytodrop : leftover.entrySet()) {
                        player.getWorld().dropItem(player.getLocation(), entitytodrop.getValue());
                    }
                    notadded ++;
                } else {
                    added ++;
                }
            }
        }

        Cooldown.put(player, System.currentTimeMillis());
        if (added > 0) {
            if (notadded == 0)
                player.sendMessage(ChatColor.AQUA + "You just picked up some items.");
            else
                player.sendMessage(ChatColor.AQUA + "You just pickup up some items, however your inventory is now full so some dropped at your feet.");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
            return true;
        } else {
            if (notadded == 0) {
                player.sendMessage(ChatColor.AQUA + "No items were picked up.");
                return false;
            } else {
                player.sendMessage(ChatColor.AQUA + "You didn't pickup any items as your inventory is full so they have been dropped at your feet.");
                return true;
            }
        }
    }

}
