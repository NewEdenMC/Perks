package co.neweden.Perks.permissions;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

public class Permissions implements Listener {

    private static Map<Permissible, PermissionAttachment> attachments = new HashMap<>();
    private static Map<Permissible, Collection<PermNodeCache>> permissionsCache = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PermissionAttachment attachment = event.getPlayer().addAttachment(Perks.getPlugion());
        attachments.put(event.getPlayer(), attachment);
        attachPermissions(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        detachPermissions(event.getPlayer());
    }

    public static void attachPermissions() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            attachPermissions(player);
        }
    }

    public static void attachPermissions(Player player) {
        for (Perk perk : Perks.getPerks(player)) {
            attachPermissions(player, perk);
        }
    }

    public static Map<String, Collection<Perk>> getPermsMap(OfflinePlayer player) {
        Validate.notNull(player, "Cannot get Permissions Map on a null OfflinePlayer object.");
        Map<String, Collection<Perk>> permsMap = new HashMap<>();
        for (Perk perk : Perks.getPerks(player)) {
            for (String perm : perk.getPermissions()) {
                if (permsMap.containsKey(perm))
                    permsMap.get(perm).add(perk);
                else
                    permsMap.put(perm, new ArrayList<>(Arrays.asList(perk)));
            }
        }
        return permsMap;
    }

    public static Map<String, Collection<Perk>> getPermsMap(Player player) {
        Validate.notNull(player, "Cannot get Permissions Map on a null Player object.");
        Map<String, Collection<Perk>> permsMap = new HashMap<>();
        for (Map.Entry<String, Boolean> perm : attachments.get(player).getPermissions().entrySet()) {
            if (!perm.getValue()) continue;
            permsMap.put(perm.getKey(), new ArrayList<Perk>());
        }
        for (PermNodeCache cache : getCache(player)) {
            permsMap.get(cache.name).addAll(cache.perks);
        }
        return permsMap;
    }

    private static Collection<PermNodeCache> getCache(Permissible permissible) {
        if (!permissionsCache.containsKey(permissible))
            permissionsCache.put(permissible, new ArrayList<PermNodeCache>());
        return permissionsCache.get(permissible);
    }

    private static PermNodeCache getCache(Permissible permissible, String perm) {
        for (PermNodeCache cache : getCache(permissible)) {
            if (cache.name.equals(perm))
                return cache;
        }
        PermNodeCache cache = new PermNodeCache(perm);
        permissionsCache.get(permissible).add(cache);
        return cache;
    }

    public static void attachPermissions(Player player, Perk perk) {
        Validate.notNull(player, "Cannot attach permissions on a null Player object");
        Validate.notNull(perk, "Cannot attach permissions using a null Perk object.");

        for (String perm : perk.getPermissions()) {
            PermNodeCache cache = getCache(player, perm);
            cache.perks.add(perk);
            attachments.get(player).setPermission(perm, true);
        }
    }

    public static void detachPermissions() {
        for (Permissible permissible : permissionsCache.keySet()) {
            detachPermissions(permissible);
        }
    }

    public static void detachPermissions(Permissible permissible) {
        Validate.notNull(permissible, "Cannot detach permissions from a null Permissible object");
        for (PermNodeCache perm : getCache(permissible)) {
            attachments.get(permissible).unsetPermission(perm.name);
        }
        permissionsCache.remove(permissible);
    }

    public static void detachPermissions(Permissible permissible, Perk perk) {
        Validate.notNull(permissible, "Cannot detach permissions from a null Permissible object");
        Validate.notNull(perk, "Cannot detach permissions using a null Perk object");

        Collection<PermNodeCache> perms = getCache(permissible);
        for (PermNodeCache perm : perms) {
            if (!perm.perks.contains(perk)) continue;
            perm.perks.remove(perk);
            if (perm.perks.isEmpty()) attachments.get(permissible).unsetPermission(perm.name);
        }
        // Cleanup
        for (PermNodeCache perm : new ArrayList<>(perms)) {
            if (perm.perks.isEmpty())
                perms.remove(perm); // remove permissions with empty Collection<Perk>
        }
    }

}
