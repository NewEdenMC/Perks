package co.neweden.perks.extras;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class Cooldown {

    private Map<CommandSender, Long> cooldowns = new HashMap<>();
    private long duration;

    public Cooldown(long duration) {
        this.duration = duration;
    }

    public long getCooldown(CommandSender sender) {
        if (!cooldowns.containsKey(sender))
            return 0;

        long end = cooldowns.get(sender);
        long now = System.currentTimeMillis() / 1000;

        if (end > now)
            return end - now;

        cooldowns.remove(sender);
        return 0;
    }

    public void setCooldown(CommandSender sender) {
        cooldowns.put(sender, (System.currentTimeMillis() / 1000) + duration);
    }

}
