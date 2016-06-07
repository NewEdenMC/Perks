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
import java.util.Collection;
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
        return loadDBConnection() && setupDB() && loadRealms() && loadPerks();
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
        reloadConfig();
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
                    "CREATE TABLE IF NOT EXISTS `realms` (\n" +
                    "  `name` varchar(64) NOT NULL,\n" +
                    "  `displayName` varchar(128) NOT NULL,\n" +
                    "  PRIMARY KEY (`name`)\n" +
                    ")"
            );
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
                    "  PRIMARY KEY (`name`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `perk_permissions` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `perkName` varchar(64) NOT NULL,\n" +
                    "  `permissionNode` varchar(256) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `players` (\n" +
                    "  `uuid` varchar(36) NOT NULL,\n" +
                    "  `balance` double NOT NULL DEFAULT 0,\n" +
                    "  PRIMARY KEY (`uuid`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `config` (\n" +
                    "  `setting` VARCHAR(128) NOT NULL,\n" +
                    "  `value` VARCHAR(256) NOT NULL,\n" +
                    "  PRIMARY KEY (`setting`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `active_perks` (\n" +
                    "  `purchaseID` INT NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` VARCHAR(36) NOT NULL,\n" +
                    "  `perkName` VARCHAR(64) NOT NULL,\n" +
                    "  `expiresOn` INT NULL,\n" +
                    "  PRIMARY KEY (`purchaseID`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `transaction_history` (\n" +
                    "  `transactionID` INT NOT NULL AUTO_INCREMENT,\n" +
                    "  `UUID` VARCHAR(36) NOT NULL,\n" +
                    "  `perkName` VARCHAR(64) NOT NULL,\n" +
                    "  `purchaseID` INT NOT NULL,\n" +
                    "  `action` VARCHAR(16) NOT NULL,\n" +
                    "  PRIMARY KEY (`transactionID`)\n" +
                    ");"
            );
            Perks.db.createStatement().executeUpdate("INSERT INTO config (`setting`) VALUES ('currency_prefix') ON DUPLICATE KEY UPDATE setting=setting;");
            Perks.db.createStatement().executeUpdate("INSERT INTO config (`setting`,`value`) VALUES ('currency_formatting','#,##0.00') ON DUPLICATE KEY UPDATE setting=setting;");
            Perks.db.createStatement().executeUpdate("INSERT INTO config (`setting`,`value`) VALUES ('currency_suffix',' credit(s)') ON DUPLICATE KEY UPDATE setting=setting;");
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Unable to setup setup database", e);
            return false;
        }
        return true;
    }

    private boolean loadRealms() {
        getLogger().info("Preparing to load realms from database");
        String configRealm = getConfig().getString("realmName", "");
        try {
            ResultSet rs = Perks.db.createStatement().executeQuery("SELECT * FROM realms;");
            while (rs.next()) {
                Realm realm = Perks.newRealm(rs.getString("name"));
                realm.setDisplayName(rs.getString("displayName"));
                if (realm.getName().equals(configRealm))
                    Perks.realm = realm;
                getLogger().info("Realm " + realm.getName() + " loaded");
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "SQLException occurred while trying to load realms from database", e);
            return false;
        }
        if (Perks.realms.isEmpty())
            getLogger().info("No realms loaded from database");
        return true;
    }

    private boolean loadPerks() {
        getLogger().info("Preparing to load perks from database");
        int topSlot = 0;
        try {
            ResultSet perks = Perks.db.createStatement().executeQuery("SELECT * FROM perks;");
            ResultSet perms = Perks.db.createStatement().executeQuery("SELECT * FROM perk_permissions;");
            while (perks.next()) {
                Perk perk = loadPerk(perks);
                if (perk == null) continue;
                if (perk.getMenuSlot() > topSlot) topSlot = perk.getMenuSlot();
                while (perms.next()) {
                    if (!perms.getString("perkName").equals(perk.getName())) continue;
                    perk.addPermission(perms.getString("permissionNode"));
                }
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
            Perks.getPerksMenu().setNumRows(rows + 1);
        }
        return true;
    }

    private Perk loadPerk(ResultSet rs) throws SQLException {
        Perk perk = Perks.newPerk(rs.getString("name"));
        if (rs.getString("displayName") != null) perk.setDisplayName(rs.getString("displayName"));
        if (rs.getString("description") != null) perk.setDescription(rs.getString("description"));
        perk.setCost(rs.getDouble("cost"));
        perk.setMenuSlot(rs.getInt("menuSlot"));
        perk.setOneTimePurchase(rs.getBoolean("oneTimePurchase"));
        perk.setTimeLength(rs.getInt("timeLength"));
        perk.setMenuMaterial(Material.getMaterial(rs.getString("menuMaterial")));
        if (rs.getBlob("menuAnimationJSON") != null) perk.setMenuAnimationJSON(rs.getBlob("menuAnimationJSON"));
        if (rs.getBlob("availableRealmsJSON") != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<Collection<String>>(){}.getType();
            String json = new String(rs.getBlob("availableRealmsJSON").getBytes(1, (int) rs.getBlob("availableRealmsJSON").length()));
            Collection<String> list = gson.fromJson(json, listType);
            for (Realm realm : Perks.getRealms()) {
                if (list.contains(realm.getName()))
                    perk.addToRealm(realm);
            }
        }
        return perk;
    }

    @EventHandler
    public void onMenuPopulate(MenuPopulateEvent event) {
        MenuInstance instance = event.getMenuInstance();
        for (Perk perk : Perks.getPerks()) {
            InventorySlot slot = instance.getSlot(perk.getMenuSlot());
            slot
                    .setMaterial(perk.getMenuMaterial())
                    .setDisplayName(perk.getDisplayName());

            if (!perk.getDescription().isEmpty())
                slot.addHoverText(perk.getDescription());
            slot.addHoverText("&lCost:&f " + Util.formatCurrency(perk.getCost()));

            if (perk.isOneTimePurchase())
                slot.addHoverText("&lDuration:&f Never expires");
            else
                slot.addHoverText("&lDuration:&f " + perk.getTimeLength());

            if (perk.isMemberOfAllRealms())
                slot.addHoverText("&lRealms:&f Can be used in all realms");
            else {
                String realms = "Can be used in ";
                int i = 0;
                for (Realm realm : perk.getMemberRealms()) {
                    realms += realm.getDisplayName();
                    if (Perks.getCurrentRealm().equals(realm)) realms += " (this realm)";
                    if (perk.getMemberRealms().size() - 1 != i) realms += ", ";
                    i++;
                }
                slot.addHoverText("&lRealms:&f " + realms);
                if (!perk.getMemberRealms().contains(Perks.getCurrentRealm()))
                    slot.addHoverText("&cNote this perk cannot be used in this realm");
            }

            String statusMessage = "";
            switch (perk.purchaseStatus(event.getOpener())) {
                case OWNS_PERK: statusMessage = "&7You already own this perk"; break;
                case HAS_ALL_PERMISSIONS: statusMessage = "&7You already own this perk"; break;
                case INSUFFICIENT_FUNDS: statusMessage = "&cYou do not have enough credit to buy this perk"; break;
                case CAN_PURCHASE: statusMessage = "&aYou can purchase this perk"; break;
            }
            slot.addHoverText(Util.formatString(statusMessage));

            if (perk.getMenuAnimationJSON() != null)
                slot.animationFromJSON(perk.getMenuAnimationJSON());
        }
        int balanceSlot = (instance.getMenu().getNumRows() * 9) - 5;
        instance.getSlot(balanceSlot)
                .setMaterial(Material.GOLD_INGOT)
                .setDisplayName("&6&lYour current balance")
                .addHoverText("&e&l" + Util.formatCurrency(Perks.getBalance(event.getOpener())));
    }

}
