package co.neweden.Perks;

import co.neweden.menugui.menu.Menu;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class Perks {

    protected static Connection db;
    protected static Main plugin;
    protected static Menu perksMenu;
    protected static Set<Perk> perks = new HashSet<>();

    public static Main getPlugion() { return plugin; }

    public static Set<Perk> getPerks() { return new HashSet<>(perks); }

    public static Perk newPerk(String perkName) {
        Perk perk = new Perk(perkName);
        perks.add(perk);
        return perk;
    }

    public static Menu getPerksMenu() { return perksMenu; }

    public static void openPerksMenu(Player player) {
        perksMenu.openMenu(player);
    }

    public static Double getBalance(OfflinePlayer player) {
        Validate.notNull(player, "Null OfflinePlayer object passed to getBalance method");
        try {
            ResultSet rs = db.createStatement().executeQuery("SELECT balance FROM players WHERE uuid='" + player.getUniqueId() + "';");
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
            db.createStatement().executeUpdate("REPLACE INTO players SET uuid='" + player.getUniqueId() + "', balance='" + newBalance + "';");
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to set the balance for " + player.getName(), e);
        }
    }

    public static String getConfigSetting(String setting) { return getConfigSetting(setting, null); }
    public static String getConfigSetting(String setting, String defValue) {
        try {
            ResultSet rs = db.createStatement().executeQuery("SELECT value FROM config WHERE setting='" + setting + "';");
            if (rs.next())
                return rs.getString("value") != null ? rs.getString("value") : defValue;
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to get the config setting '" + setting + "'", e);
        }
        return defValue;
    }

    public static void setConfigSetting(String setting, String value) {
        try {
            db.createStatement().executeUpdate("REPLACE INTO config SET setting='" + setting + "', value='" + value + "';");
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to set the config setting '" + setting + "' to '" + value + "'", e);
        }
    }

}
