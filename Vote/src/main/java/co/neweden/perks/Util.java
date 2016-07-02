package co.neweden.perks;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

    public static BaseComponent[] formatStringToBaseComponent(String text) {
        text = formatString(text);
        return TextComponent.fromLegacyText(text);
    }

    public static String formatCurrency(Double value) {
        String prefix = pluralise(value, Perks.getConfigSetting("currency_prefix", ""));
        String suffix = pluralise(value, Perks.getConfigSetting("currency_suffix", ""));
        DecimalFormat df = new DecimalFormat(Perks.getConfigSetting("currency_formatting", "#,##0.00"));
        return prefix + df.format(value) + suffix;
    }

    public static String formatTime(long timeInSeconds, TimeUnit limitTo) { return formatTime(timeInSeconds, limitTo, true); }
    public static String formatTime(long timeInSeconds, TimeUnit limitTo, boolean shortTimeLabels) {
        String out = "";

        long days = TimeUnit.SECONDS.toDays(timeInSeconds);
        out += days > 0 ? days + (shortTimeLabels ? "d " : pluralise(days, " day(s) ")) : "";
        if (limitTo.equals(TimeUnit.DAYS)) return out.substring(0, out.length() - 1);
        timeInSeconds -= TimeUnit.DAYS.toSeconds(days);

        long hours = TimeUnit.SECONDS.toHours(timeInSeconds);
        out += hours > 0 ? hours + (shortTimeLabels ? "h " : pluralise(hours, " hour(s) ")) : "";
        if (limitTo.equals(TimeUnit.HOURS)) return out.substring(0, out.length() - 1);
        timeInSeconds -= TimeUnit.HOURS.toSeconds(hours);

        long minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds);
        out += minutes > 0 ? minutes + (shortTimeLabels ? "m " : pluralise(minutes, " minute(s) ")) : "";
        if (limitTo.equals(TimeUnit.MINUTES)) return out.substring(0, out.length() - 1);
        timeInSeconds -= TimeUnit.MINUTES.toSeconds(minutes);

        long seconds = TimeUnit.SECONDS.toSeconds(timeInSeconds);
        out += seconds > 0 ? seconds + (shortTimeLabels ? "s" : pluralise(seconds, " second(s) ")) : "";
        return out;
    }

    public static String pluralise(Number number, String string) {
        if (number.doubleValue() == 1)
            return string.replaceAll("(?i)" + Pattern.quote("(s)"), "");
        else
            return string.replaceAll("(?i)" + Pattern.quote("(s)"), "s");
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
