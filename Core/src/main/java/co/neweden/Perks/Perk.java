package co.neweden.Perks;

import org.bukkit.Material;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Perk {

    private String name;
    private String displayName;
    private String description = "";
    private Double cost;
    private Integer menuSlot;
    private Material menuMaterial;
    private String menuAnimationJSON;
    private Boolean oneTimePurchase;
    private Integer timeLength;
    private Collection<String> realms = new ArrayList<>();
    private Collection<String> permissions = new ArrayList<>();

    public Perk(String perkName) { name = perkName; }

    public String getName() { return name; }

    public Perk setDisplayName(String displayName) { this.displayName = displayName; return this; }
    public String getDisplayName() { return displayName != null ? displayName : name; }

    public Perk setDescription(String description) { this.description = description; return this; }
    public String getDescription() { return description; }

    public Perk setCost(Double cost) { this.cost = cost; return this; }
    public Double getCost() { return cost; }

    public Perk setMenuSlot(Integer slot) { menuSlot = slot; return this; }
    public Integer getMenuSlot() { return menuSlot; }

    public Perk setMenuMaterial(Material material) { menuMaterial = material; return this; }
    public Material getMenuMaterial() { return menuMaterial; }

    public Perk setMenuAnimationJSON(String json) { menuAnimationJSON = json; return this; }
    public Perk setMenuAnimationJSON(Blob json) throws SQLException {
        menuAnimationJSON = new String(json.getBytes(1, (int) json.length()));
        return this;
    }
    public String getMenuAnimationJSON() { return menuAnimationJSON; }

    public Perk setOneTimePurchase(boolean isOneTimePurchase) { oneTimePurchase = isOneTimePurchase; return this; }
    public Boolean isOneTimePurchase() { return oneTimePurchase; }

    public Perk setTimeLength(Integer timeLength) { this.timeLength = timeLength; return this; }
    public Integer getTimeLength() { return timeLength; }

    public Perk addRealm(String realmName) { realms.add(realmName); return this; }
    public Perk addRealms(Collection<String> realmNames) { realms.addAll(realmNames); return this; }
    public Collection<String> getRealms() { return new ArrayList<>(realms); }

    public Perk addPermission(String permissionNode) { permissions.add(permissionNode); return this; }
    public Perk addPermissions(Collection<String> permissionNodes) { permissions.addAll(permissionNodes); return this; }
    public Collection<String> getPermissions() { return new ArrayList<>(permissions); }

}
