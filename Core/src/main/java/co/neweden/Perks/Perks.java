package co.neweden.Perks;

import co.neweden.menugui.menu.Menu;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

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

}
