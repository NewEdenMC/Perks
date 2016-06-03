package co.neweden.Perks;

import co.neweden.menugui.menu.Menu;
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

    public static Double getBalance(Player player) {
        try {
            ResultSet rs = db.createStatement().executeQuery("SELECT balance FROM players WHERE uuid='" + player.getUniqueId() + "';");
            if (rs.next())
                return rs.getDouble("balance");
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to get the balance for " + player.getName(), e);
        }
        return 0D;
    }

    public static void setBalance(Player player, Double newBalance) {
        try {
            db.createStatement().executeUpdate("REPLACE INTO players SET uuid='" + player.getUniqueId() + "', balance='" + newBalance + "';");
        } catch (SQLException e) {
            getPlugion().getLogger().log(Level.SEVERE, "An SQL Exception occurred while trying to set the balance for " + player.getName(), e);
        }
    }

}
