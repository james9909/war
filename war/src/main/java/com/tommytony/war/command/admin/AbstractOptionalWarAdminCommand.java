package com.tommytony.war.command.admin;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.command.regular.AbstractWarCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


/**
 * Represents a command that may only be used by War admins
 */
public abstract class AbstractOptionalWarAdminCommand extends AbstractWarCommand {

    public AbstractOptionalWarAdminCommand(WarCommandHandler handler, CommandSender sender, String[] args, boolean mustBeWarAdmin) throws NotWarAdminException {
        super(handler, sender, args);

        if (mustBeWarAdmin && !isSenderWarAdmin()) {
            throw new NotWarAdminException();
        }
    }

    public boolean isSenderWarAdmin() {
        if (this.getSender() instanceof Player) {
            return War.war.isWarAdmin((Player) this.getSender());
        }
        return this.getSender() instanceof ConsoleCommandSender;
    }
}
