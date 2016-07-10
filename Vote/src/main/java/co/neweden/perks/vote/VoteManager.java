package co.neweden.perks.vote;

import co.neweden.perks.Perks;
import co.neweden.perks.Util;
import co.neweden.perks.transactions.Transaction;
import co.neweden.perks.transactions.Transactions;
import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class VoteManager implements Listener {

    private static Collection<VoteService> voteServices = new ArrayList<>();
    private static Set<String> recentVotes = new HashSet<>();
    private static int recentTotalVotes = 0;
    private static boolean anonymousVotes = false;
    private static ScheduledTask scheduler;

    public VoteManager() { schedule(); }

    private static void schedule() {
        scheduler = Perks.getPlugion().getProxy().getScheduler().schedule(Perks.getPlugion(), () -> {
            broadcastRecentVotes();
        }, 0L, Perks.getConfigSetting("vote_broadcast_frequency", 120), TimeUnit.SECONDS);
    }

    public static Collection<VoteService> getVoteServices() { return new ArrayList<>(voteServices); }

    public static boolean buildVoteServicesCache() {
        voteServices.clear();
        Perks.getPlugion().getLogger().info("Attempting to load Vote Services");
        try {
            ResultSet rs = Perks.getDB().prepareStatement("SELECT * FROM vote_services;").executeQuery();
            while (rs.next()) {
                VoteService vs = new VoteService(rs.getInt("id"));
                vs.serviceName = rs.getString("serviceName");
                vs.displayName = rs.getString("displayName");
                vs.showOnVoteList = rs.getBoolean("showOnVoteList");
                vs.voteURL = rs.getString("voteURL");
                vs.currencyPerVote = rs.getDouble("currencyPerVote");
                voteServices.add(vs);
                Perks.getPlugion().getLogger().info("Loaded " + vs.serviceName + " (" + vs.displayName + ")");
            }
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException has occurred while getting vote service information", e);
            return false;
        }
        if (voteServices.size() == 0)
            Perks.getPlugion().getLogger().log(Level.INFO, "No vote services loaded");
        return true;
    }


    protected static boolean setValue(int id, String key, String value) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("UPDATE vote_services SET " + key + "=? WHERE id=?"); // key not sanitized but should never be user generated
            st.setObject(1, value);
            st.setInt(2, id);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while updating vote services " + id, e);
            return false;
        }
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        VoteService vs = null;
        for (VoteService site : getVoteServices()) {
            if (event.getVote().getServiceName().equals(site.getServiceName()))
                vs = site;
        }
        if (vs == null) {
            Perks.getPlugion().getLogger().warning("Votifier passed a vote from service '" + event.getVote().getServiceName() + "' at '" + event.getVote().getAddress() + "', however the service has not been registered with Perks, so this vote will not be processed.");
            return;
        }
        vs.incrementTotalVotes();

        if (event.getVote().getUsername().isEmpty())
            anonymousVotes = true;
        else
            recentVotes.add(event.getVote().getUsername());

        recentTotalVotes++;

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(event.getVote().getUsername());
        UUID uuid;
        if (player != null)
            uuid = player.getUniqueId();
        else
            uuid = Util.getUUID(event.getVote().getUsername());

        if (uuid != null) {

            Transaction t = Transactions.newTransaction(Transactions.Type.VOTE, uuid);
            t.setVoteService(vs);
            Perks.setBalance(uuid, Perks.getBalance(uuid) + vs.getCurrencyPerVote());
            t.setStatus(Transactions.Status.COMPLETE);

            try {
                PreparedStatement st = Perks.getDB().prepareStatement("UPDATE players SET lastVote=? WHERE uuid=?;\n");
                st.setLong(1, System.currentTimeMillis() / 1000);
                st.setString(2, uuid.toString());
                st.executeUpdate();
            } catch (SQLException e) {
                Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception ocurred while setting lastVote for player " + uuid, e);
            }

            if (player != null)
                player.sendMessage(Util.formatStringToBaseComponent("&7Thanks for voting! You have earned &c" + Util.formatCurrency(vs.getCurrencyPerVote()) + "&7, type &c/vote&7 to vote again, and &c/perks&7 to see what you can buy."));

        }

        if (recentTotalVotes >= 10)
            broadcastRecentVotes();
    }

    private static void broadcastRecentVotes() {
        if (recentTotalVotes < 1) return;

        String names = "";
        if (!recentVotes.isEmpty()) {
            if (anonymousVotes) recentVotes.add("others");
            names = " from &e&l" + Util.formatListToString(recentVotes);
        }

        Perks.getPlugion().getProxy().broadcast(Util.formatStringToBaseComponent(
                "&a\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\n" +
                " \n" +
                "&e&l" + recentTotalVotes + "&f&l " + Util.pluralise(recentTotalVotes, "vote(s)") + " recently" + names + "&f&l, type &e&l/vote&f&l to vote and earn " + Perks.getConfigSetting("currency_reference_name", "money") + " to spend on perks\n" +
                " \n" +
                "&a\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580\u2580"
        ));

        scheduler.cancel();
        schedule();
        recentVotes.clear();
        recentTotalVotes = 0;
        anonymousVotes = false;
    }

    public static void clearLocalCache() {
        voteServices.clear();
        recentVotes.clear();
        recentTotalVotes = 0;
        anonymousVotes = false;
    }

}
