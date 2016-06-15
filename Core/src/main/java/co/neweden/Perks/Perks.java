package co.neweden.Perks;

import co.neweden.menugui.menu.Menu;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

public class Perks {

    protected static Connection db;
    protected static Main plugin;
    protected static Realm realm;
    protected static Collection<Realm> realms = new ArrayList<>();
    protected static Menu realmsMenu;
    protected static Collection<Perk> perks = new ArrayList<>();

    public static Main getPlugion() { return plugin; }

    public static Connection getDB() { return db; }

    public static Realm getCurrentRealm() { return realm; }

    public static Collection<Realm> getRealms() { return new ArrayList<>(realms); }

    public static Realm newRealm(String realmName) {
        Realm realm = new Realm(realmName);
        realms.add(realm);
        return realm;
    }

    public static Menu getRealmsMenu() { return realmsMenu; }

    public static Collection<Perk> getPerks() { return new ArrayList<>(perks); }

    public static Collection<Perk> getPerks(OfflinePlayer player) {
        Collection<Perk> perks = new ArrayList<>();
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT perkName FROM active_perks WHERE uuid=?;");
            st.setString(1, player.getUniqueId().toString());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                for (Perk perk : getPerks()) {
                    if (rs.getString("perkName").equals(perk.getName()))
                        perks.add(perk);
                }
            }
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while trying to get active perks for " + player.getName(), e);
        }
        return perks;
    }

    public static Perk newPerk(String perkName) {
        Perk perk = new Perk(perkName);
        perks.add(perk);
        return perk;
    }

    protected static boolean setValue(String perkName, String key, String value) {
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("UPDATE perks SET " + key + "=? WHERE name=?"); // key not sanitized but should never be user generated
            st.setObject(1, value);
            st.setString(2, perkName);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while updating the perk " + perkName, e);
            return false;
        }
    }

    public static Double getBalance(OfflinePlayer player) {
        Validate.notNull(player, "Null OfflinePlayer object passed to getBalance method");
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT balance FROM players WHERE uuid=?;");
            st.setString(1, player.getUniqueId().toString());
            ResultSet rs = st.executeQuery();
            if (rs.next())
                return rs.getDouble("balance");
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to get the balance for " + player.getName(), e);
        }
        return 0D;
    }

    public static void setBalance(OfflinePlayer player, Double newBalance) {
        Validate.notNull(player, "Null OfflinePlayer object passed to setBalance method");
        Validate.notNull(newBalance, "Null Double object passed to setBalance method for newBalance");
        try {
            PreparedStatement st = Perks.getDB().prepareStatement("REPLACE INTO players SET uuid=?, balance=?;");
            st.setString(1, player.getUniqueId().toString());
            st.setDouble(2, newBalance);
            st.executeUpdate();
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to set the balance for " + player.getName(), e);
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
