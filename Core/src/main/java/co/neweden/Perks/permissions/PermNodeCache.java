package co.neweden.Perks.permissions;

import co.neweden.Perks.Perk;

import java.util.ArrayList;
import java.util.Collection;

public class PermNodeCache {

    String name;
    Collection<Perk> perks = new ArrayList<>();

    PermNodeCache(String name) {
        this.name = name;
    }

}
