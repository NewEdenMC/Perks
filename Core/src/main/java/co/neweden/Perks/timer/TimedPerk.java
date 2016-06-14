package co.neweden.Perks.timer;

import co.neweden.Perks.Perk;
import org.bukkit.entity.Player;

public class TimedPerk {

    Perk perk;
    Player player;
    long expire;

    TimedPerk(Perk perk, Player player, long expire) {
        this.perk = perk;
        this.player = player;
        this.expire = expire;
    }

}
