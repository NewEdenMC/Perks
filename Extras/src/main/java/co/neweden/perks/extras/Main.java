package co.neweden.perks.extras;

import co.neweden.perks.extras.cmd.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        new BitchSlap();
        new BroFist();
        new HighFive();
        new Prefix();
        new RainbowSheep();
        new Slap();
    }

    public static Main getPlugin() { return plugin; }

}
