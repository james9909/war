package com.tommytony.war.command.admin;

import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;


/**
 * Represents a command that may only be used by War admins
 */
public abstract class AbstractWarAdminCommand extends AbstractOptionalWarAdminCommand {

    public AbstractWarAdminCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotWarAdminException {
        super(handler, sender, args, true);
    }
}
