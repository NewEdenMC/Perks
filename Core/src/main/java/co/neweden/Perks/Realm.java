package co.neweden.Perks;

public class Realm {

    protected String name;
    protected String displayName;

    private Realm() { }
    protected Realm(String name) { this.name = name; }

    public String getName() { return name; }

    public String getDisplayName() { return displayName != null ? displayName : name; }
    public Realm setDisplayName(String displayName) { this.displayName = displayName; return this; }

}
