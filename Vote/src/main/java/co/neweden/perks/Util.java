package co.neweden.perks;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.regex.Pattern;

public class Util {

    private Util() { }

    public static String formatString(String text) {
        text = text.replaceAll("&0", "\u00A70"); // Black
        text = text.replaceAll("&1", "\u00A71"); // Dark Blue
        text = text.replaceAll("&2", "\u00A72"); // Dark Green
        text = text.replaceAll("&3", "\u00A73"); // Dark Aqua
        text = text.replaceAll("&4", "\u00A74"); // Dark Red
        text = text.replaceAll("&5", "\u00A75"); // Dark Purple
        text = text.replaceAll("&6", "\u00A76"); // Gold
        text = text.replaceAll("&7", "\u00A77"); // Gray
        text = text.replaceAll("&8", "\u00A78"); // Dark Gray
        text = text.replaceAll("&9", "\u00A79"); // Blue
        text = text.replaceAll("&a", "\u00A7a"); // Green
        text = text.replaceAll("&b", "\u00A7b"); // Aqua
        text = text.replaceAll("&c", "\u00A7c"); // Red
        text = text.replaceAll("&d", "\u00A7d"); // Light Purple
        text = text.replaceAll("&e", "\u00A7e"); // Yellow
        text = text.replaceAll("&f", "\u00A7f"); // White

        text = text.replaceAll("&k", "\u00A7k"); // Obfuscated
        text = text.replaceAll("&l", "\u00A7l"); // Bold
        text = text.replaceAll("&m", "\u00A7m"); // Strikethrough
        text = text.replaceAll("&o", "\u00A7o"); // Italic
        text = text.replaceAll("&r", "\u00A7r"); // Reset

        return text;
    }

    public static String formatCurrency(Double value) {
        String prefix = Perks.getConfigSetting("currency_prefix", "");
        if (value == 1)
            prefix = prefix.replaceAll("(?i)" + Pattern.quote("(s)"), "");
        else
            prefix = prefix.replaceAll("(?i)" + Pattern.quote("(s)"), "s");

        String suffix = Perks.getConfigSetting("currency_suffix", "");
        if (value == 1)
            suffix = suffix.replaceAll("(?i)" + Pattern.quote("(s)"), "");
        else
            suffix = suffix.replaceAll("(?i)" + Pattern.quote("(s)"), "s");

        DecimalFormat df = new DecimalFormat(Perks.getConfigSetting("currency_formatting", "#,##0.00"));
        return prefix + df.format(value) + suffix;
    }

    public static UUID getUUID(String name) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.getName().equals(name))
                return player.getUniqueId();
        }

        HttpProfileRepository hpr = new HttpProfileRepository("minecraft");
        Profile[] p = hpr.findProfilesByNames(name);
        if (p.length > 0) {
            String uuid = p[0].getId().replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
            return UUID.fromString(uuid);
        } else
            return null;
    }

}
