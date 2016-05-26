package co.neweden.Perks;

import co.neweden.menugui.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        Perks.plugin = this;
        startup();
        getCommand("perks").setExecutor(new Commands());
    }

    private boolean startup() {
        saveDefaultConfig();
        Perks.perksMenu = MenuGUI.newMenu("perks");
        return loadDBConnection();
    }

    public boolean reload() {
        if (!MenuGUI.unloadMenu(Perks.perksMenu)) return false;
        try {
            Perks.db.close();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Unable to close database connection", e);
            return false;
        }
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

}
