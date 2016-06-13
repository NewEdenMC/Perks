package co.neweden.Perks.commands;

import org.bukkit.command.*;

public class InsufficientPermissionsException extends CommandException {

    InsufficientPermissionsException() { super("You do not have permission to perform that command."); }

    InsufficientPermissionsException(String message) { super(message); }

    InsufficientPermissionsException(String message, Throwable cause) { super(message, cause); }

}
