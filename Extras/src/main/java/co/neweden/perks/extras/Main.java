package co.neweden.perks.extras;

import co.neweden.perks.extras.cmd.BitchSlap;
import co.neweden.perks.extras.cmd.BroFist;
import co.neweden.perks.extras.cmd.HIghFive;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        new BitchSlap();
        new BroFist();
        new HIghFive();
    }

    public static Main getPlugin() { return plugin; }

}
