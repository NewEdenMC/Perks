package co.neweden.Perks.transactions;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Transaction {

    protected int transactionID;
    private Transactions.Type type;
    private OfflinePlayer player;
    protected Perk perk;
    protected int purchaseID;
    protected long timeStamp;
    protected Transactions.Status status;

    private Transaction() { }

    protected Transaction(Transactions.Type type, OfflinePlayer player) {
        Validate.notNull(type, "Cannot create a new Transaction with a null Transaction.Type");
        this.type = type;
        Validate.notNull(player, "Cannot create a new Transaction with a null OfflinePlayer");
        this.player = player;
    }

    protected int createTransaction() throws SQLException {
        timeStamp = System.currentTimeMillis() / 1000;
        status = Transactions.Status.OPEN;
        PreparedStatement st = Perks.getDB().prepareStatement("INSERT INTO `transaction_history` (`type`, `UUID`, `timeStamp`, `status`) VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
        st.setString(1, type.toString());
        st.setString(2, player.getUniqueId().toString());
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

    public OfflinePlayer getPlayer() { return player; }

    public Perk getPerk() { return perk; }
    public boolean setPerk(Perk perk) {
        if (!Transactions.setValue(transactionID, "perkName", perk.getName())) return false;
        this.perk = perk;
        return true;
    }

    public int getPurchaseID() { return purchaseID; }
    public boolean setPurchaseID(int purchaseID) {
        if (!Transactions.setValue(transactionID, "purchaseID", String.valueOf(purchaseID))) return false;
        this.purchaseID = purchaseID;
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
