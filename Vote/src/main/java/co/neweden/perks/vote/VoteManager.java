package co.neweden.perks.vote;

import co.neweden.perks.Perks;
import co.neweden.perks.Util;
import co.neweden.perks.transactions.Transaction;
import co.neweden.perks.transactions.Transactions;
import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

public class VoteManager implements Listener {

    private static Collection<VoteService> voteServices = new ArrayList<>();
    private static Collection<String> recentVotes = new ArrayList<>();
    private static int recentTotalVotes = 0;

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

        if (event.getVote().getUsername() != null) {
            if (!recentVotes.contains(event.getVote().getUsername()))
                recentVotes.add(event.getVote().getUsername());
        }
        recentTotalVotes++;

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(event.getVote().getUsername());
        UUID uuid = player.getUniqueId();

        Transaction t = Transactions.newTransaction(Transactions.Type.VOTE, uuid);
        t.setVoteService(vs);
        Perks.setBalance(uuid, Perks.getBalance(uuid) + vs.getCurrencyPerVote());
        t.setStatus(Transactions.Status.COMPLETE);
        player.sendMessage(Util.formatString("&aThanks for voting on '" + vs.getDisplayName() + "' you have earned " + Util.formatCurrency(vs.getCurrencyPerVote()) + ", type /vote to see where else you may be able to vote."));
    }

    public static void clearLocalCache() { voteServices.clear(); }

}
