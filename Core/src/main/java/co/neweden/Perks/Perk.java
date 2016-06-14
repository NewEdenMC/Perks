package co.neweden.Perks;

import co.neweden.Perks.permissions.Permissions;
import co.neweden.Perks.timer.Timer;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

public class Perk {

    private String name;
    private String displayName;
    private String description = "";
    private Double cost;
    private Integer menuSlot;
    private Material menuMaterial;
    private String menuAnimationJSON;
    private Integer timeLength;
    private Collection<Realm> realms = new ArrayList<>();
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

    public Perk setTimeLength(Integer timeLength) { this.timeLength = timeLength; return this; }
    public Integer getTimeLength() { return timeLength; }

    public Perk addToRealm(Realm realm) { realms.add(realm); return this; }
    public Perk addToRealms(Collection<Realm> realm) { realms.addAll(realm); return this; }
    public Collection<Realm> getMemberRealms() {
        return isMemberOfAllRealms() ? Perks.getRealms() : new ArrayList<>(realms);
    }
    public boolean isMemberOfAllRealms() { return realms.isEmpty(); }

    public Perk addPermission(String permissionNode) { permissions.add(permissionNode); return this; }
    public Perk addPermissions(Collection<String> permissionNodes) { permissions.addAll(permissionNodes); return this; }
    public Collection<String> getPermissions() { return new ArrayList<>(permissions); }

    public enum PurchaseStatus { OWNS_PERK, HAS_ALL_PERMISSIONS, INSUFFICIENT_FUNDS, CAN_PURCHASE }

    public PurchaseStatus purchaseStatus(Player player) {
        Validate.notNull(player, "Player to check for cannot be null");

        try {
            ResultSet rs = Perks.db.createStatement().executeQuery("SELECT purchaseID FROM active_perks WHERE perkName='" + getName() + "' AND uuid='" + player.getUniqueId() + "';");
            if (rs.next())
                return PurchaseStatus.OWNS_PERK;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while trying to get purcahse information.", e);
        }
        boolean hasAll = true;
        for (String perm : getPermissions()) {
            if (!player.hasPermission(perm))
                hasAll = false;
        }
        if (hasAll)
            return PurchaseStatus.HAS_ALL_PERMISSIONS;

        if (Perks.getBalance(player) < getCost())
            return PurchaseStatus.INSUFFICIENT_FUNDS;

        return PurchaseStatus.CAN_PURCHASE;
    }

    public boolean purchasePerk(Player player) {
        if (purchaseStatus(player) != PurchaseStatus.CAN_PURCHASE) return false;
        Perks.setBalance(player, Perks.getBalance(player) - getCost());

        try {
            int purchaseID;
            Statement st = Perks.db.createStatement();
            st.executeUpdate("INSERT INTO `active_perks` (`uuid`, `perkName`, `purchaseTimeStamp`) VALUES ('" + player.getUniqueId() + "', '" + getName() + "', '" + System.currentTimeMillis() / 1000 + "');", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                purchaseID = rs.getInt(1);
            } else return false;

            Perks.db.createStatement().executeUpdate("INSERT INTO `transaction_history` (`UUID`, `perkName`, `purchaseID`, `action`, `timeStamp`) VALUES ('" + player.getUniqueId() + "', '" + getName() + "', '" + purchaseID + "', 'PURCHASE', '" + System.currentTimeMillis() / 1000 + "');");
            Permissions.attachPermissions(player, this);
            if (getTimeLength() >= 0)
                Timer.addTimedPerk(this, player);
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while purchasing a perk.", e);
            return false;
        }
        return true;
    }

    public enum RemoveStatus { EXPIRE, REFUND, REMOVED }

    public boolean removePerk(Player player, RemoveStatus status) {
        Validate.notNull(player, "Cannot remove perk from a null Player object");
        if (status == null) status = RemoveStatus.REMOVED;

        PurchaseStatus ps = purchaseStatus(player);
        if (ps == PurchaseStatus.HAS_ALL_PERMISSIONS || ps != PurchaseStatus.OWNS_PERK)
            return false;

        try {
            int purchaseID;
            ResultSet rs = Perks.db.createStatement().executeQuery("SELECT purchaseID FROM active_perks WHERE uuid='" + player.getUniqueId() + "' AND perkName='" + getName() + "';");
            if (!rs.next())
                return false;
            purchaseID = rs.getInt("purchaseID");

            Perks.db.createStatement().executeLargeUpdate("DELETE FROM `active_perks` WHERE uuid='" + player.getUniqueId() + "' AND perkName='" + getName() + "';");
            Perks.db.createStatement().executeUpdate("INSERT INTO `transaction_history` (`UUID`, `perkName`, `purchaseID`, `action`, `timeStamp`) VALUES ('" + player.getUniqueId() + "', '" + getName() + "', '" + purchaseID + "', '" + status + "', '" + System.currentTimeMillis() / 1000 + "');");

            Permissions.detachPermissions(player, this);
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while removing a perk.", e);
            return false;
        }
        if (status == RemoveStatus.REFUND)
            Perks.setBalance(player, Perks.getBalance(player) + getCost());
        return true;
    }

}
