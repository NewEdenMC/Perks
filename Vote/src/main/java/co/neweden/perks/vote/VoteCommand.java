package co.neweden.perks.vote;

import co.neweden.perks.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;

public class VoteCommand extends Command {

    public VoteCommand() {
        super("vote");
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        sender.sendMessage(Util.formatStringToBaseComponent("&b&lHere are a list of services you can vote on, click on each one in the list to vote!\n"));
        Collection<VoteService> services = VoteManager.getVoteServices();

        boolean canVote = false;
        for (VoteService vs : services) {
            if (!vs.isShowOnVoteList()) continue;
            TextComponent text = new TextComponent("- ");
            text.addExtra(vs.getDisplayName());
            text.setColor(ChatColor.WHITE);
            text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, vs.getVoteURL()));
            sender.sendMessage(text);
            canVote = true;
        }
        if (!canVote)
            sender.sendMessage(Util.formatStringToBaseComponent("&7No voting services available :("));

        sender.sendMessage(Util.formatStringToBaseComponent("\n&bBy voting you will earn credits which can be used to buy perks, type /perks for more info."));
    }

}
