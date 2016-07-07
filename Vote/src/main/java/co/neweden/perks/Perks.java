package co.neweden.perks;

import org.apache.commons.lang.Validate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class Perks {

    protected static Connection db;
    protected static Main plugin;

    public static Main getPlugion() { return plugin; }

    public static Connection getDB() { return db; }

    public static Double getBalance(UUID uuid) {
        Validate.notNull(uuid, "Null UUID object passed to getBalance method");
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT balance FROM players WHERE uuid=?;");
            st.setString(1, uuid.toString());
            ResultSet rs = st.executeQuery();
            if (rs.next())
                return rs.getDouble("balance");
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to get the balance for " + uuid, e);
        }
        return 0D;
    }

    public static void setBalance(UUID uuid, Double newBalance) {
        Validate.notNull(uuid, "Null UUID object passed to setBalance method");
        Validate.notNull(newBalance, "Null Double object passed to setBalance method for newBalance");
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("REPLACE INTO players SET uuid=?, balance=?;");
            st.setString(1, uuid.toString());
            st.setDouble(2, newBalance);
            st.executeUpdate();
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to set the balance for " + uuid, e);
        }
    }

    public static String getConfigSetting(String setting) { return getConfigSetting(setting, null); }
    public static String getConfigSetting(String setting, String defValue) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT value FROM config WHERE setting=?;");
            st.setString(1, setting);
            ResultSet rs = st.executeQuery();
            if (rs.next())
                return rs.getString("value") != null ? rs.getString("value") : defValue;
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to get the config setting '" + setting + "'", e);
        }
        return defValue;
    }

    private static long getConfigSetting(String setting, long defValue) {
        try {
            return Long.parseLong(Perks.getConfigSetting(String.valueOf(setting), String.valueOf(defValue)));
        } catch (NumberFormatException e) {
            Perks.getPlugion().getLogger().log(Level.WARNING, "Tried to get config value " + setting + ", but value is not a number: " + e.getMessage());
        }
        return defValue;
    }

    public static void setConfigSetting(String setting, String value) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("REPLACE INTO config SET setting=?, value=?;");
            st.setString(1, setting);
            st.setString(2, value);
            st.executeUpdate();
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to set the config setting '" + setting + "' to '" + value + "'", e);
        }
    }

}
