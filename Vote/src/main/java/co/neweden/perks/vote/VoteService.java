package co.neweden.perks.vote;

import co.neweden.perks.Perks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class VoteService {

    private int id;
    protected String serviceName;
    protected String displayName;
    protected boolean showOnVoteList;
    protected String voteURL;
    protected double currencyPerVote;

    private VoteService() { }

    protected VoteService(int id) { this.id = id;}

    public int getID() { return id; }

    public String getServiceName() { return serviceName; }
    public boolean setServiceName(String serviceName) {
        if (!VoteManager.setValue(id, "serviceName", serviceName)) return false;
        this.serviceName = serviceName;
        return true;
    }

    public String getDisplayName() { return displayName != null ? displayName : serviceName; }
    public boolean setDisplayName(String displayName) {
        if (!VoteManager.setValue(id, "displayName", displayName)) return false;
        this.displayName = displayName;
        return true;
    }

    public boolean isShowOnVoteList() { return showOnVoteList; }
    public boolean setShowOnVoteList(boolean show) {
        if (!VoteManager.setValue(id, "showOnVoteList", String.valueOf(show ? 1 : 0))) return false;
        showOnVoteList = show;
        return true;
    }

    public String getVoteURL() { return voteURL; }
    public boolean setVoteURL(String voteURL) {
        if (!VoteManager.setValue(id, "voteURL", voteURL)) return false;
        this.voteURL = voteURL;
        return true;
    }

    public double getCurrencyPerVote() { return currencyPerVote; }
    public boolean setCurrencyPerVote(double amount) {
        if (!VoteManager.setValue(id, "currencyPerVote", String.valueOf(amount))) return false;
        currencyPerVote = amount;
        return true;
    }

    public int getTotalVotes() {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT totalVotes FROM vote_services WHERE id=?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next())
                return rs.getInt("totalVotes");
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException has occurred while trying to get total votes for service " + id, e);
        }
        return 0;
    }

    public boolean incrementTotalVotes() { return incrementTotalVotes(1); }
    public boolean incrementTotalVotes(int amount) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("UPDATE vote_services SET totalVotes = totalVotes + ? WHERE id=?");
            st.setInt(1, amount);
            st.setInt(2, id);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException has occurred while trying to increment total votes for service " + id, e);
            return false;
        }
    }

}
