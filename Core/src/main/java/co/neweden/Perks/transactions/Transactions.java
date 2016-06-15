package co.neweden.Perks.transactions;

import co.neweden.Perks.Perk;
import co.neweden.Perks.Perks;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

    public static Transaction newTransaction(Type type, OfflinePlayer player) {
        Transaction t = new Transaction(type, player);
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
            ResultSet rs = Perks.getDB().createStatement().executeQuery("SELECT * FROM transaction_history WHERE transactionID='" + transactionID + "';");
            if (!rs.next())
                return null;
            Type type = Type.valueOf(rs.getString("type"));
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("UUID")));
            Transaction t = new Transaction(type, player);
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
            Perks.getDB().createStatement().executeUpdate("UPDATE `transaction_history` SET `" + key + "`='" + value + "' WHERE `transactionID`='" + transactionID + "';");
            return true;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while updating a transaction.");
            return false;
        }
    }

    public static void clearLocalCache() { transactions.clear(); }

}
