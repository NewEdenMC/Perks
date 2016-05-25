package co.neweden.Perks;

import co.neweden.menugui.MenuGUI;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        Perks.plugin = this;
        Perks.perksMenu = MenuGUI.newMenu("perks");
    }

}
