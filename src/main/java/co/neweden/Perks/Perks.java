package co.neweden.Perks;

import co.neweden.menugui.menu.Menu;
import org.bukkit.entity.Player;

public class Perks {

    protected static Main plugin;
    protected static Menu perksMenu;

    public static Main getPlugion() { return plugin; }

    public static Menu getPerksMenu() { return perksMenu; }

    public static void openPerksMenu(Player player) {
        perksMenu.openMenu(player);
    }

}
