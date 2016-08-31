package co.neweden.perks.vote;

import co.neweden.perks.Perks;
import co.neweden.perks.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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

        sender.sendMessage(Util.formatStringToBaseComponent("\n&bBy voting you will earn " + Perks.getConfigSetting("currency_reference_name", "money") + " which can be used to buy perks, type /perks for more info."));

        if (!(sender instanceof ProxiedPlayer))
            return;

        ProxiedPlayer player = (ProxiedPlayer) sender;

        try {
            PreparedStatement st = Perks.getDB().prepareStatement("SELECT lastVote FROM players WHERE uuid=?");
            st.setString(1, player.getUniqueId().toString());
            ResultSet rs = st.executeQuery();
            rs.next();

            if (rs.getLong("lastVote") <= 0) return;
            long lastVote = System.currentTimeMillis() / 1000 - rs.getLong("lastVote");
            sender.sendMessage(Util.formatStringToBaseComponent("\n&fYou last voted &b" + Util.formatTime(lastVote, TimeUnit.MINUTES, false) + "&f ago"));
        } catch (SQLException e) {
            Perks.getPlugion().getLogger().log(Level.SEVERE, "Unable to get lastVote timestamp from database for UUID " + player.getUniqueId(), e);
        }

    }

}
