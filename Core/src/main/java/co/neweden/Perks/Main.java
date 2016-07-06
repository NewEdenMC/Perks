package co.neweden.Perks;

import co.neweden.Perks.commands.CommandMain;
import co.neweden.Perks.commands.HelpPages;
import co.neweden.Perks.commands.PerkCommands;
import co.neweden.Perks.commands.PlayerCommands;
import co.neweden.Perks.permissions.Permissions;
import co.neweden.Perks.timer.Timer;
import co.neweden.Perks.transactions.Transactions;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Perks.plugin = this;
        startup();
        HelpPages.registerClass(CommandMain.class);
        HelpPages.registerClass(PerkCommands.class);
        HelpPages.registerClass(PlayerCommands.class);
        getCommand("perks").setExecutor(new CommandMain());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Permissions(), this);
        getServer().getPluginManager().registerEvents(new Timer(), this);
    }

    private boolean startup() {
        saveDefaultConfig();
        if (!loadDBConnection() || !setupDB() || !loadRealms() || !loadPerks())
            return false;
        Permissions.attachPermissions();
        Timer.timer();
        return true;
    }

    public boolean reload() {
        if (!MenuGUI.unloadMenu(Perks.getRealmsMenu())) return false;
        for (Realm realm : Perks.getRealms()) {
            if (!MenuGUI.unloadMenu(realm.getPerksMenu())) return false;
        }
        Permissions.detachPermissions();
        Perks.realms.clear();
        Perks.perks.clear();
        Timer.reset();
        Transactions.clearLocalCache();
        try {
            Perks.db.close();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Unable to close database connection", e);
            return false;
        }
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
                    "  `timeLength` INT(11) NOT NULL DEFAULT -1,\n" +
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
                    "  `lastVote` INT(11) NOT NULL DEFAULT 0,\n" +
                    "  PRIMARY KEY (`uuid`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `config` (\n" +
                    "  `setting` VARCHAR(128) NOT NULL,\n" +
                    "  `value` VARCHAR(256) NULL,\n" +
                    "  PRIMARY KEY (`setting`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `active_perks` (\n" +
                    "  `purchaseID` INT NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` VARCHAR(36) NOT NULL,\n" +
                    "  `perkName` VARCHAR(64) NOT NULL,\n" +
                    "  `purchaseTimeStamp` INT NULL,\n" +
                    "  PRIMARY KEY (`purchaseID`)\n" +
                    ");"
            );
            Perks.db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `transaction_history` (\n" +
                    "  `transactionID` INT NOT NULL AUTO_INCREMENT,\n" +
                    "  `type` VARCHAR(16) NOT NULL,\n" +
                    "  `UUID` VARCHAR(36) NOT NULL,\n" +
                    "  `perkName` VARCHAR(64) NULL,\n" +
                    "  `purchaseID` INT NULL,\n" +
                    "  `voteService` INT NULL,\n" +
                    "  `timeStamp` INT NOT NULL,\n" +
                    "  `status` VARCHAR(16) NOT NULL,\n" +
                    "  PRIMARY KEY (`transactionID`)\n" +
                    ");"
            );
            PreparedStatement stS = Perks.getDB().prepareStatement("INSERT INTO config (`setting`) VALUES (?) ON DUPLICATE KEY UPDATE setting=setting;");
            PreparedStatement stSV = Perks.getDB().prepareStatement("INSERT INTO config (`setting`,`value`) VALUES (?,?) ON DUPLICATE KEY UPDATE setting=setting;");

            stS.setString(1, "currency_prefix");
            stS.executeUpdate();

            stSV.setString(1, "currency_formatting");
            stSV.setString(2, "#,##0.00");
            stSV.executeUpdate();

            stSV.setString(1, "currency_suffix");
            stSV.setString(2, "credit(s)");
            stSV.executeUpdate();

            stSV.setString(1, "currency_reference_name");
            stSV.setString(2, "credits");
            stSV.executeUpdate();
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
            ResultSet rs = Perks.getDB().prepareStatement("SELECT * FROM realms;").executeQuery();
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
        Perks.realmsMenu = MenuGUI.newMenu("perks_realms");
        Perks.getRealmsMenu().setTitle("Realms Selector for Perks");
        Perks.getRealmsMenu().setNumRows(((9 - (Perks.getRealms().size() % 9)) + Perks.getRealms().size()) / 9);
        return true;
    }

    @EventHandler
    public void onRealmsMenuPopulate(MenuPopulateEvent event) {
        if (!Perks.getRealmsMenu().equals(event.getMenuInstance().getMenu())) return;
        int slotI = 0;
        for (Realm realm : Perks.getRealms()) {
            event.getMenuInstance().getSlot(slotI)
                    .setMaterial(Material.GRASS)
                    .setDisplayName(realm.getDisplayName())
                    .addHoverText("&7Click to see Perks for this realm")
                    .setClickCommand("perks " + realm.getName());
            slotI++;
        }
    }

    private boolean loadPerks() {
        getLogger().info("Preparing to load perks from database");
        try {
            ResultSet perks = Perks.getDB().prepareStatement("SELECT * FROM perks;").executeQuery();
            ResultSet perms = Perks.getDB().prepareStatement("SELECT * FROM perk_permissions;").executeQuery();
            while (perks.next()) {
                Perk perk = loadPerk(perks);
                if (perk == null) continue;
                while (perms.next()) {
                    if (!perms.getString("perkName").equals(perk.getName())) continue;
                    perk.addPermission(perms.getString("permissionNode"));
                }
                perms.beforeFirst();
                getLogger().info("Perk " + perk.getName() + " loaded");
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "SQLException occurred while trying to load perks from database", e);
            return false;
        }
        if (Perks.perks.isEmpty())
            getLogger().info("No perks loaded from database");
        else {
            for (Realm realm : Perks.getRealms()) {
                realm.configurePerksMenu();
            }
        }
        return true;
    }

    private Perk loadPerk(ResultSet rs) throws SQLException {
        Perk perk = Perks.newPerk(rs.getString("name"));
        if (rs.getString("displayName") != null) perk.displayName = rs.getString("displayName");
        if (rs.getString("description") != null) perk.description = rs.getString("description");
        perk.cost = rs.getDouble("cost");
        perk.menuSlot = rs.getInt("menuSlot");
        perk.timeLength = rs.getInt("timeLength");
        perk.menuMaterial = Material.getMaterial(rs.getString("menuMaterial"));
        if (rs.getBlob("menuAnimationJSON") != null)
            perk.menuAnimationJSON = new String(rs.getBlob("menuAnimationJSON").getBytes(1, (int) rs.getBlob("menuAnimationJSON").length()));
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
    public void onPerksMenuPopulate(MenuPopulateEvent event) {
        Realm realm = null;
        for (Realm r : Perks.getRealms()) {
            if (r.getPerksMenu().equals(event.getMenuInstance().getMenu()))
                realm = r;
        }
        if (realm == null) return;
        MenuInstance instance = event.getMenuInstance();

        for (Perk perk : Perks.getPerks()) {
            if (!perk.getMemberRealms().contains(realm))
                continue;

            InventorySlot slot = instance.getSlot(perk.getMenuSlot());
            slot
                    .setMaterial(perk.getMenuMaterial())
                    .setDisplayName(perk.getDisplayName());

            if (!perk.getDescription().isEmpty()) {
                slot.addHoverText("&7" + perk.getDescription());
                slot.addHoverText("&7 ");
            }
            slot.addHoverText("&lCost:&b " + Util.formatCurrency(perk.getCost()));

            if (perk.getTimeLength() == -1)
                slot.addHoverText("&lDuration:&b Never expires");
            else
                slot.addHoverText("&lDuration:&b " + Util.formatTime(perk.getTimeLength(), TimeUnit.MINUTES));

            if (perk.isMemberOfAllRealms())
                slot.addHoverText("&lRealms:&b Can be used in all realms");
            else {
                String realms = "Can be used in ";
                int i = 0;
                for (Realm r : perk.getMemberRealms()) {
                    realms += r.getDisplayName();
                    if (realm.equals(Perks.getCurrentRealm())) realms += " (this realm)";
                    if (perk.getMemberRealms().size() - 1 != i) realms += ", ";
                    i++;
                }
                slot.addHoverText("&lRealms:&b " + realms);
            }

            if (!perk.getMemberRealms().contains(Perks.getCurrentRealm())) {
                slot.addHoverText("&eWarning: this perk cannot be used in '" + Perks.getCurrentRealm().getDisplayName() + "' which is the realm you are currently in");
            }

            String statusMessage = "";
            Perk.PurchaseStatus ps = perk.purchaseStatus(event.getOpener());
            switch (ps) {
                case OWNS_PERK: statusMessage = "&7You already own this perk"; break;
                case HAS_ALL_PERMISSIONS: statusMessage = "&7You automatically have this perk based on your current permissions"; break;
                case INSUFFICIENT_FUNDS: statusMessage = "&cYou do not have enough " + Perks.getConfigSetting("currency_reference_name", "money") + " to buy this perk"; break;
                case CAN_PURCHASE: statusMessage = "&aYou can purchase this perk"; break;
            }
            slot.addHoverText("&f ");
            slot.addHoverText(Util.formatString(statusMessage));
            if (ps.equals(Perk.PurchaseStatus.CAN_PURCHASE))
                slot.setClickCommand("perks perk " + perk.getName() + " buy");

            if (perk.getMenuAnimationJSON() != null)
                slot.animationFromJSON(perk.getMenuAnimationJSON());
        }
        int balanceSlot = (instance.getMenu().getNumRows() * 9) - 5;
        instance.getSlot(balanceSlot)
                .setMaterial(Material.GOLD_INGOT)
                .setDisplayName("&6&lYour current balance")
                .addHoverText("&e&l" + Util.formatCurrency(Perks.getBalance(event.getOpener())));
        instance.getSlot(balanceSlot - 1)
                .setMaterial(Material.NETHER_STAR)
                .setDisplayName("&bVoting")
                .addHoverText("&7To earn " + Perks.getConfigSetting("currency_reference_name", "money") + " you need to vote for us on websites, usually you can vote once per day on each site.")
                .addHoverText("&cType /vote to see where you can vote.");
    }

}
