package co.neweden.Perks;

import co.neweden.menugui.*;
import co.neweden.menugui.menu.InventorySlot;
import co.neweden.menugui.menu.MenuInstance;
import co.neweden.menugui.menu.MenuPopulateEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Type;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Perks.plugin = this;
        startup();
        getCommand("perks").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(this, this);
    }

    private boolean startup() {
        saveDefaultConfig();
        Perks.perksMenu = MenuGUI.newMenu("perks");
        return loadDBConnection() && setupDB() && loadPerks();
    }

    public boolean reload() {
        if (!MenuGUI.unloadMenu(Perks.perksMenu)) return false;
        try {
            Perks.db.close();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Unable to close database connection", e);
            return false;
        }
        MenuGUI.unloadMenu(Perks.getPerksMenu());
        return startup();
    }

    private boolean loadDBConnection() {
        String host = getConfig().getString("mysql.host", null);
        String port = getConfig().getString("mysql.port", null);
        String database = getConfig().getString("mysql.database", null);
        if (host == null || port == null || database == null) {
            getLogger().log(Level.INFO, "No database information received from config.");
            return false;
        }

        String url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);

        try {
            Perks.db = DriverManager.getConnection(url, getConfig().getString("mysql.user", ""), getConfig().getString("mysql.password", ""));
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "An SQLException occurred while trying to connect to the database.", e);
            return false;
        }
        getLogger().log(Level.INFO, "Connected to MySQL Database");
        return true;
    }

    private boolean setupDB() {
        try {
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `perks` (\n" +
                    "  `name` VARCHAR(64) NOT NULL,\n" +
                    "  `displayName` VARCHAR(64) NULL,\n" +
                    "  `description` VARCHAR(256) NULL,\n" +
                    "  `cost` DOUBLE NOT NULL DEFAULT 0,\n" +
                    "  `menuSlot` INT NOT NULL,\n" +
                    "  `oneTimePurchase` TINYINT(1) NOT NULL DEFAULT 1,\n" +
                    "  `timeLength` INT(11) NULL,\n" +
                    "  `menuMaterial` VARCHAR(128) NOT NULL,\n" +
                    "  `menuAnimationJSON` BLOB NULL,\n" +
                    "  `availableRealmsJSON` BLOB NULL,\n" +
                    "  PRIMARY KEY (`name`)" +
                    ");"
            );
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Unable to setup setup database", e);
            return false;
        }
        return true;
    }

    private boolean loadPerks() {
        getLogger().info("Preparing to load perks from database");
        ResultSet rs;
        int topSlot = 0;
        try {
            rs = Perks.db.createStatement().executeQuery("SELECT * FROM perks;");
            while (rs.next()) {
                Perk perk = loadPerk(rs);
                if (perk == null) continue;
                if (perk.getMenuSlot() > topSlot) topSlot = perk.getMenuSlot();
                getLogger().info("Perk " + perk.getName() + " loaded");
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "SQLException occurred while trying to load perks from database", e);
            return false;
        }
        if (Perks.perks.isEmpty())
            getLogger().info("No perks loaded from database");
        else {
            int rows = ((9 - (topSlot % 9)) + topSlot) / 9;
            Perks.getPerksMenu().setNumRows(rows);
        }
        return true;
    }

    private Perk loadPerk(ResultSet rs) throws SQLException {
        Perk perk = Perks.newPerk(rs.getString("name"));
        if (rs.getString("displayName") != null) perk.setDisplayName(rs.getString("displayName"));
        if (rs.getString("description") != null) perk.setDisplayName(rs.getString("description"));
        perk.setCost(rs.getDouble("cost"));
        perk.setMenuSlot(rs.getInt("menuSlot"));
        perk.setOneTimePurchase(rs.getBoolean("oneTimePurchase"));
        perk.setTimeLength(rs.getInt("timeLength"));
        perk.setMenuMaterial(Material.getMaterial(rs.getString("menuMaterial")));
        if (rs.getBlob("menuAnimationJSON") != null) perk.setMenuAnimationJSON(rs.getBlob("menuAnimationJSON"));
        if (rs.getBlob("availableRealmsJSON") != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            String json = new String(rs.getBlob("availableRealmsJSON").getBytes(1, (int) rs.getBlob("availableRealmsJSON").length()));
            List<String> list = gson.fromJson(json, listType);
            perk.addRealms(list);
        }
        return perk;
    }

    @EventHandler
    public void onMenuPopulate(MenuPopulateEvent event) {
        MenuInstance i = event.getMenuInstance();
        for (Perk perk : Perks.getPerks()) {
            InventorySlot slot = i.getSlot(perk.getMenuSlot());
            slot
                    .setMaterial(perk.getMenuMaterial())
                    .setDisplayName(perk.getDisplayName())
                    .addHoverText("&lCost:&f " + perk.getCost());

            if (!perk.getDescription().isEmpty())
                slot.addHoverText(perk.getDescription());

            if (perk.isOneTimePurchase())
                slot.addHoverText("&lDuration:&f Never expires");
            else
                slot.addHoverText("&lDuration:&f " + perk.getTimeLength());

            if (perk.getRealms().isEmpty())
                slot.addHoverText("&lRealms:&f Can be used in all realms");
            else
                slot.addHoverText("&lRealms:&f Can only be used in " + perk.getRealms().toString());

            if (perk.getMenuAnimationJSON() != null)
                slot.animationFromJSON(perk.getMenuAnimationJSON());
        }
    }

}
