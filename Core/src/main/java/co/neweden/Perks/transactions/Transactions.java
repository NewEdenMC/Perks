package co.neweden.Perks.transactions;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

public class Transactions {

    public enum Type { PURCHASE, EXPIRE, REFUND, REMOVED }
    public enum Status { OPEN, COMPLETE }
    private static Collection<Transaction> transactions = new ArrayList<>();

    public static Transaction newTransaction(Type type, UUID uuid) {
        Transaction t = new Transaction(type, uuid);
        try {
            t.createTransaction();
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while trying to create a new transaction.", e);
            return null;
        }
        transactions.add(t);
        return t;
    }

    public static Transaction getTransaction(int transactionID) {
        for (Transaction t : transactions) {
            if (t.getTransactionID() == transactionID)
                return t;
        }
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT * FROM transaction_history WHERE transactionID=?;");
            st.setInt(1, transactionID);
            ResultSet rs = st.executeQuery();
            if (!rs.next())
                return null;
            Type type = Type.valueOf(rs.getString("type"));
            Transaction t = new Transaction(type, UUID.fromString(rs.getString("UUID")));
            t.transactionID = rs.getInt("transactionID");
            if (rs.getString("perkName") != null) {
                for (Perk perk : Perks.getPerks()) {
                    if (perk.getName().equals(rs.getString("perkName")))
                        t.perk = perk;
                }
            }
            t.purchaseID = rs.getInt("purchaseID");
            t.status = Status.valueOf(rs.getString("status"));
            return t;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while trying to get a transaction.", e);
            return null;
        }
    }

    protected static boolean setValue(int transactionID, String key, String value) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("UPDATE transaction_history SET " + key + "=? WHERE transactionID=?"); // key not sanitized but should never be user generated
            st.setObject(1, value);
            st.setInt(2, transactionID);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while updating transaction " + transactionID, e);
            return false;
        }
    }

    public static void clearLocalCache() { transactions.clear(); }

}
