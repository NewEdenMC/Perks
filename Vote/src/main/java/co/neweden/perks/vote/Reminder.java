package co.neweden.perks.vote;

import co.neweden.perks.Perks;
import co.neweden.perks.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Reminder implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        Perks.getPlugion().getProxy().getScheduler().schedule(Perks.getPlugion(), () -> {
            remindPlayer(event.getPlayer(), getReminderPeriod());
        }, 3L, TimeUnit.SECONDS);
    }

    public static void scheduleReminders() {
        Perks.getPlugion().getProxy().getScheduler().schedule(Perks.getPlugion(), () -> {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                remindPlayer(player, getReminderPeriod());
            }
        }, 0L, 1L, TimeUnit.MINUTES);
    }

    private static long getReminderPeriod() {
        try {
            return Long.parseLong(Perks.getConfigSetting("vote_reminder_period", "86400"));
        } catch (NumberFormatException e) {
            Perks.getPlugion().getLogger().log(Level.WARNING, "Tried to get config value vote_reminder_period, but value is not a number: " + e.getMessage());
        }
        return 86400L;
    }

    private static void remindPlayer(ProxiedPlayer player, long reminderPeriod) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT lastVote FROM players WHERE uuid=?");
            st.setString(1, player.getUniqueId().toString());
            ResultSet rs = st.executeQuery();
            if (!rs.next()) return;
            if (rs.getLong("lastVote") + reminderPeriod > System.currentTimeMillis() / 1000) return;
            player.sendMessage(Util.formatStringToBaseComponent(
                    "&a\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\n" +
                    " \n" +
                    "&f&lThe last time you voted was over " + Util.formatTime(reminderPeriod, TimeUnit.SECONDS, false) + " ago, to vote again and earn more credits for perks, type /vote and vote on the available services.\n" +
                    " \n" +
                    "&a\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580"
            ));
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while getting lastVote for player " + player.getUniqueId(), e);
        }

    }

}
