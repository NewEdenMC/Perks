package co.neweden.Perks.commands;

import co.neweden.Perks.Util;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HelpPages {

    private static Collection<Class> classes = new ArrayList<>();
    private static Map<String, Collection<SubCommandHelp>> subCommands = new HashMap<>();

    public static void registerClass(Class c) { classes.add(c); }

    public static void renderHelpPage(CommandSender sender) { renderHelpPage(sender, ""); }

    public static void renderHelpPage(CommandSender sender, String pageName){
        if (subCommands.isEmpty())
            buildPageCache();

        if (!subCommands.containsKey(pageName)) return;

        sender.sendMessage(Util.formatString("&fAvailable sub-commands"));
        for (SubCommandHelp subCmd : subCommands.get(pageName)) {
            if (!subCmd.permission().isEmpty() && !sender.hasPermission(subCmd.permission())) continue;
            sender.sendMessage(Util.formatString("&f- " + subCmd.usage() + ": &b" + subCmd.description()));
        }
    }

    public static void buildPageCache() {
        subCommands.clear();
        for (Class c : classes) {
            for (Method m : c.getMethods()) {
                for (SubCommandHelp subCmd : m.getAnnotationsByType(SubCommandHelp.class)) {
                    if (!subCommands.containsKey(subCmd.helpPage()))
                        subCommands.put(subCmd.helpPage(), new ArrayList<>());
                    subCommands.get(subCmd.helpPage()).add(subCmd);
                }
            }
        }
    }

}
