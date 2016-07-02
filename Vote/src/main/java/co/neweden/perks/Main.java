package co.neweden.perks;

import co.neweden.perks.vote.Reminder;
import co.neweden.perks.vote.VoteCommand;
import co.neweden.perks.vote.VoteManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class Main extends Plugin {

    private Configuration config;

    @Override
    public void onEnable() {
        Perks.plugin = this;
        startup();
        ProxyServer.getInstance().getPluginManager().registerListener(this, new VoteManager());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new Reminder());
        getProxy().getPluginManager().registerCommand(this, new VoteCommand());
        Reminder.scheduleReminders();
    }

    private boolean startup() {
        return !loadConfig() || !loadDBConnection() || !VoteManager.buildVoteServicesCache();
    }

    private boolean loadConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    int c;
                    while ((c = is.read()) != -1) {
                        os.write(c);
                    }
                }
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            if (config == null) {
                getLogger().log(Level.SEVERE, "Could not load config file, ConfigurationProvider is null.");
                return false;
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "IO Exception occurred while getting Perks config.", e);
            return false;
        }
        return true;
    }

    public Configuration getConfig() { return config; }

    private boolean loadDBConnection() {
        String host = getConfig().getString("mysql.host", null);
        int port = getConfig().getInt("mysql.port", 0);
        String database = getConfig().getString("mysql.database", null);
        if (host == null || port == 0 || database == null) {
            getLogger().log(Level.INFO, "No database information received from config or some information was not valid");
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
                    "CREATE TABLE IF NOT EXISTS `vote_services` (\n" +
                    "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                    "  `serviceName` VARCHAR(64) NOT NULL,\n" +
                    "  `displayName` VARCHAR(64) NULL,\n" +
                    "  `showOnVoteList` TINYINT(1) NOT NULL DEFAULT 1,\n" +
                    "  `voteURL` VARCHAR(512) NULL,\n" +
                    "  `currencyPerVote` DOUBLE NOT NULL DEFAULT 0,\n" +
                    "  `totalVotes` INT NOT NULL DEFAULT 0,\n" +
                    "  PRIMARY KEY (`id`));\n"
            );
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Unable to setup setup database", e);
            return false;
        }
        return true;
    }

}
