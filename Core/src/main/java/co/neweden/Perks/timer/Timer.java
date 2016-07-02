package co.neweden.Perks.timer;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;
import co.neweden.Perks.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

public class Timer implements Listener {

    private static Collection<TimedPerk> perks = new ArrayList<>();
    private static BukkitTask task;

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Perk perk : Perks.getPerks(event.getPlayer())) {
            if (perk.getTimeLength() > -1)
                addTimedPerk(perk, event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (TimedPerk perk : new ArrayList<>(perks)) {
            if (perk.player.equals(event.getPlayer()))
                perks.remove(perk);
        }
    }

    public static boolean addTimedPerk(Perk perk, Player player) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT purchaseTimeStamp FROM active_perks WHERE perkName=? AND uuid=?;");
            st.setString(1, perk.getName());
            st.setString(2, player.getUniqueId().toString());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                long currentTime = System.currentTimeMillis() / 1000;
                long expire = rs.getInt("purchaseTimeStamp") + perk.getTimeLength();
                if (currentTime >= expire) {
                    perk.removePerk(player, Perk.RemoveStatus.EXPIRE);
                    player.sendMessage(Util.formatString("&eThe perk '" + perk.getDisplayName() + "' expired while you were offline, you can purchase it again"));
                    return false;
                }
                perks.add(new TimedPerk(perk, player, expire));
                return true;
            }
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception has occurred while trying to get active perk data.", e);
        }
        return false;
    }

    public static boolean removeTimedPerk(Perk perk, Player player) {
        for (TimedPerk tPerk : new ArrayList<>(perks)) {
            if (tPerk.perk.equals(perk) && tPerk.player.equals(player)) {
                perks.remove(tPerk);
                return true;
            }
        }
        return false;
    }

    public static void reset() {
        perks.clear();
        task.cancel();
    }

    public static void timer() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis() / 1000;
                for (TimedPerk perk : new ArrayList<>(perks)) {
                    if (currentTime < perk.expire) continue;
                    if (!perk.perk.removePerk(perk.player, Perk.RemoveStatus.EXPIRE)) continue;
                    perks.remove(perk);
                    perk.player.sendMessage(Util.formatString(
                            "&a\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\n" +
                            " \n" +
                            "&f&lThe perk &e&l" + perk.perk.getDisplayName() + "&f&l has now expired, you can now purchase it again\n" +
                            " \n" +
                            "&a\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580"
                    ));
                }
            }
        }.runTaskTimer(Perks.getPlugion(), 0L, 300L);
    }

}
