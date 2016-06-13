package co.neweden.Perks.commands;

import org.bukkit.command.CommandException;

public class UnknownSubCommandException extends CommandException {

    UnknownSubCommandException() { super("Unknown sub-command"); }

    UnknownSubCommandException(String message) { super(message); }

    UnknownSubCommandException(String message, Throwable cause) { super(message, cause); }

}
