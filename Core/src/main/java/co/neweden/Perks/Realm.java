package co.neweden.Perks;

import co.neweden.menugui.MenuGUI;
import co.neweden.menugui.menu.Menu;

public class Realm {

    protected String name;
    protected String displayName;
    private Menu menu;

    private Realm() { }
    protected Realm(String name) {
        this.name = name;
        menu = MenuGUI.newMenu("perks_" + name);
    }

    public String getName() { return name; }

    public String getDisplayName() { return displayName != null ? displayName : name; }
    public Realm setDisplayName(String displayName) { this.displayName = displayName; return this; }

    public Menu getPerksMenu() { return menu; }

    protected void configurePerksMenu() {
        menu.setTitle(getDisplayName() + " Perks");
        int topSlot = 0;
        for (Perk perk : Perks.getPerks()) {
            if (perk.getMemberRealms().contains(this) && perk.getMenuSlot() > topSlot) {
                topSlot = perk.getMenuSlot();
            }
        }
        int rows = ((9 - (topSlot % 9)) + topSlot) / 9;
        getPerksMenu().setNumRows(rows + 1);
    }

}
