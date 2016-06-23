package co.neweden.perks.transactions;

import co.neweden.perks.Perks;
import co.neweden.perks.vote.VoteService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Transaction {

    protected int transactionID;
    private Transactions.Type type;
    private UUID uuid;
    protected VoteService voteService;
    protected long timeStamp;
    protected Transactions.Status status;

    private Transaction() { }

    protected Transaction(Transactions.Type type, UUID uuid) {
        //Validate.notNull(type, "Cannot create a new Transaction with a null Transaction.Type");
        this.type = type;
        //Validate.notNull(uuid, "Cannot create a new Transaction with a null UUID");
        // Validate doesn't get to exist because IntelliJ's being a penis
        this.uuid = uuid;
    }

    protected int createTransaction() throws SQLException {
        timeStamp = System.currentTimeMillis() / 1000;
        status = Transactions.Status.OPEN;
        PreparedStatement st = Perks.getDB().prepareStatement("INSERT INTO `transaction_history` (`type`, `UUID`, `timeStamp`, `status`) VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
        st.setString(1, type.toString());
        st.setString(2, uuid.toString());
        st.setLong(3, timeStamp);
        st.setString(4, status.toString());
        st.executeUpdate();
        ResultSet rs = st.getGeneratedKeys();
        rs.next();
        transactionID = rs.getInt(1);
        return transactionID;
    }

    public int getTransactionID() { return transactionID; }

    public Transactions.Type getType() { return type; }
    public boolean setType(Transactions.Type type) {
        if (!Transactions.setValue(transactionID, "type", type.toString())) return false;
        this.type = type;
        return true;
    }

    public UUID getUUID() { return uuid; }

    public VoteService getVoteService() { return voteService; }
    public boolean setVoteService(VoteService voteService) {
        if (!Transactions.setValue(transactionID, "voteService", String.valueOf(voteService.getID()))) return false;
        this.voteService = voteService;
        return true;
    }

    public long getTimeStamp() { return timeStamp; }

    public Transactions.Status getStatus() { return status; }
    public boolean setStatus(Transactions.Status status) {
        if (!Transactions.setValue(transactionID, "status", status.toString())) return false;
        this.status = status;
        return true;
    }

}
