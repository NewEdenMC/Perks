package co.neweden.Perks;

import co.neweden.Perks.permissions.Permissions;
import co.neweden.Perks.timer.Timer;
import co.neweden.Perks.transactions.Transaction;
import co.neweden.Perks.transactions.Transactions;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

public class Perk {

    private String name;
    protected String displayName;
    protected String description = "";
    protected Double cost;
    protected Integer menuSlot;
    protected Material menuMaterial;
    protected String menuAnimationJSON;
    protected Integer timeLength;
    protected Collection<Realm> realms = new ArrayList<>();
    protected Collection<String> permissions = new ArrayList<>();

    private Perk() { }

    protected Perk(String perkName) { name = perkName; }

    public String getName() { return name; }

    public String getDisplayName() { return displayName != null ? displayName : name; }
    public Perk setDisplayName(String displayName) {
        Perks.setValue(name, "displayName", displayName);
        this.displayName = displayName;
        return this;
    }

    public String getDescription() { return description; }
    public Perk setDescription(String description) {
        Perks.setValue(name, "description", description);
        this.description = description;
        return this;
    }

    public Double getCost() { return cost; }
    public Perk setCost(Double cost) {
        Perks.setValue(name, "cost", cost.toString());
        this.cost = cost;
        return this;
    }

    public Integer getTimeLength() { return timeLength; }
    public Perk setTimeLength(Integer timeLength) {
        Perks.setValue(name, "timeLength", timeLength.toString());
        this.timeLength = timeLength;
        return this;
    }

    public Integer getMenuSlot() { return menuSlot; }
    public Perk setMenuSlot(Integer slot) {
        Perks.setValue(name, "menuSlot", slot.toString());
        menuSlot = slot;
        return this;
    }

    public Material getMenuMaterial() { return menuMaterial; }
    public Perk setMenuMaterial(Material material) {
        Perks.setValue(name, "menuMaterial", material.toString());
        menuMaterial = material;
        return this;
    }

    public String getMenuAnimationJSON() { return menuAnimationJSON; }
    public Perk setMenuAnimationJSON(String json) {
        menuAnimationJSON = json;
        return this;
    }
    public Perk setMenuAnimationJSON(Blob json) throws SQLException {
        menuAnimationJSON = new String(json.getBytes(1, (int) json.length()));
        return this;
    }

    public Collection<Realm> getMemberRealms() {
        return isMemberOfAllRealms() ? Perks.getRealms() : new ArrayList<>(realms);
    }
    public Perk addToRealm(Realm realm) { realms.add(realm); return this; }
    public Perk addToRealms(Collection<Realm> realm) {
        realms.addAll(realm);
        return this;
    }

    public boolean isMemberOfAllRealms() { return realms.isEmpty(); }

    public Collection<String> getPermissions() { return new ArrayList<>(permissions); }
    public Perk addPermission(String permissionNode) { permissions.add(permissionNode); return this; }
    public Perk addPermissions(Collection<String> permissionNodes) { permissions.addAll(permissionNodes); return this; }

    public enum PurchaseStatus { OWNS_PERK, HAS_ALL_PERMISSIONS, INSUFFICIENT_FUNDS, CAN_PURCHASE }

    public PurchaseStatus purchaseStatus(Player player) {
        Validate.notNull(player, "Player to check for cannot be null");

        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT purchaseID FROM active_perks WHERE perkName=? AND uuid=?;");
            st.setString(1, getName());
            st.setString(2, player.getUniqueId().toString());
            ResultSet rs = st.executeQuery();
            if (rs.next())
                return PurchaseStatus.OWNS_PERK;
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while trying to get purchase information.", e);
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

        Transaction transaction = Transactions.newTransaction(Transactions.Type.PURCHASE, player);
        transaction.setPerk(this);
        try {
            PreparedStatement st = Perks.db.prepareStatement("INSERT INTO `active_perks` (`uuid`, `perkName`, `purchaseTimeStamp`) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            st.setString(1, player.getUniqueId().toString());
            st.setString(2, getName());
            st.setLong(3, System.currentTimeMillis() / 1000);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (!rs.next())
                return false;
            transaction.setPurchaseID(rs.getInt(1));
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while purchasing a perk.", e);
            return false;
        }
        Permissions.attachPermissions(player, this);
        if (getTimeLength() >= 0)
            Timer.addTimedPerk(this, player);
        transaction.setStatus(Transactions.Status.COMPLETE);
        return true;
    }

    public enum RemoveStatus { EXPIRE, REFUND, REMOVED }

    public boolean removePerk(Player player, RemoveStatus status) {
        Validate.notNull(player, "Cannot remove perk from a null Player object");
        if (status == null) status = RemoveStatus.REMOVED;

        PurchaseStatus ps = purchaseStatus(player);
        if (ps == PurchaseStatus.HAS_ALL_PERMISSIONS || ps != PurchaseStatus.OWNS_PERK)
            return false;

        Transaction transaction = Transactions.newTransaction(Transactions.Type.valueOf(status.toString()), player);
        transaction.setPerk(this);
        try {
            PreparedStatement stSel = Perks.getDB().prepareStatement("SELECT purchaseID FROM active_perks WHERE uuid=? AND perkName=?;");
            stSel.setString(1, player.getUniqueId().toString());
            stSel.setString(2, getName());
            ResultSet rs = stSel.executeQuery();
            if (!rs.next())
                return false;
            int purchaseID = rs.getInt("purchaseID");

            PreparedStatement stDel = Perks.getDB().prepareStatement("DELETE FROM `active_perks` WHERE purchaseID=?;");
            stDel.setInt(1, purchaseID);
            stDel.executeUpdate();
            transaction.setPurchaseID(purchaseID);
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "An SQLException occurred while removing a perk.", e);
            return false;
        }
        Permissions.detachPermissions(player, this);
        if (status == RemoveStatus.REFUND)
            Perks.setBalance(player, Perks.getBalance(player) + getCost());
        transaction.setStatus(Transactions.Status.COMPLETE);
        return true;
    }

}
