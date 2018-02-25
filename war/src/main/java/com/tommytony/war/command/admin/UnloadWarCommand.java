package com.tommytony.war.command.admin;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;


/**
 * Unloads war.
 *
 * @author Tim Düsterhus
 */
public class UnloadWarCommand extends AbstractWarAdminCommand {

    public UnloadWarCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotWarAdminException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (this.args.length != 0) {
            return false;
        }

        War.war.unloadWar();
        this.msg("War unloaded.");
        return true;
    }
}
