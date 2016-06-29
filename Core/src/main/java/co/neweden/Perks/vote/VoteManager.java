package co.neweden.Perks.vote;

import co.neweden.Perks.Perks;
import co.neweden.Perks.Util;
import co.neweden.Perks.transactions.Transaction;
import co.neweden.Perks.transactions.Transactions;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

public class VoteManager implements Listener {

    private static Collection<VoteSite> voteSites = new ArrayList<>();

    public static Collection<VoteSite> getVoteSites() { return new ArrayList<>(voteSites); }

    public static boolean buildVoteSitesCache() {
        voteSites.clear();
        try {
            ResultSet rs = Perks.getDB().prepareStatement("SELECT * FROM vote_services;").executeQuery();
            while (rs.next()) {
                VoteSite vs = new VoteSite(rs.getInt("id"));
                vs.serviceName = rs.getString("serviceName");
                vs.displayName = rs.getString("displayName");
                vs.showOnVoteList = rs.getBoolean("showOnVoteList");
                vs.voteURL = rs.getString("voteURL");
                vs.currencyPerVote = rs.getDouble("currencyPerVote");
                voteSites.add(vs);
            }
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException has occurred while getting vote service information", e);
            return false;
        }
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
        VoteSite vs = null;
        for (VoteSite site : getVoteSites()) {
            if (event.getVote().getServiceName().equals(site.getServiceName()))
                vs = site;
        }
        if (vs == null) {
            Perks.getPlugion().getLogger().warning("Votifier passed a vote from service '" + event.getVote().getServiceName() + "' at '" + event.getVote().getAddress() + "', however the service has not been registered with Perks, so this vote will not be processed.");
            return;
        }
        vs.incrementTotalVotes();
        OfflinePlayer player = Util.getOfflinePlayer(event.getVote().getUsername());
        Transaction t = Transactions.newTransaction(Transactions.Type.VOTE, player);
        t.setVoteSite(vs);
        if (player != null) {
            Bukkit.broadcastMessage(Util.formatString("&c" + event.getVote().getUsername() + "&7 earned &c" + Util.formatCurrency(vs.getCurrencyPerVote()) + "&7 by voting on &c" + vs.getDisplayName()));
            Perks.setBalance(player, Perks.getBalance(player) + vs.getCurrencyPerVote());
            t.setStatus(Transactions.Status.COMPLETE);
        }
    }

    public static void clearLocalCache() { voteSites.clear(); }

}
