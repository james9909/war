package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.command.admin.AbstractOptionalWarAdminCommand;
import com.tommytony.war.command.admin.NotWarAdminException;
import com.tommytony.war.config.bags.WarConfigBag;
import org.bukkit.command.CommandSender;

public class SetWarConfigCommand extends AbstractOptionalWarAdminCommand {

    public SetWarConfigCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotWarAdminException {
        super(handler, sender, args, false);
    }

    @Override
    public boolean handle() {
        boolean wantsToPrint = false;
        if (this.args.length == 0) {
            return false;
        } else if (this.args.length == 1 && (this.args[0].equals("-p") || this.args[0].equals("print"))) {
            String config = War.war.printConfig();
            this.msg(config);
            return true;
        } else if (this.args.length > 1 && (this.args[0].equals("-p") || this.args[0].equals("print"))) {
            wantsToPrint = true;
        }

        if (!this.isSenderWarAdmin()) {
            War.war.badMsg(this.getSender(), "You can't do this if you are not a War admin (permission war.admin).");
            return true;
        }

        String namedParamReturn = War.war.updateFromNamedParams(this.getSender(), this.args);
        if (!namedParamReturn.equals("") && !namedParamReturn.equals("PARSE-ERROR")) {
            WarConfigBag.afterUpdate(this.getSender(), namedParamReturn, wantsToPrint);
        } else if (namedParamReturn.equals("PARSE-ERROR")) {
            this.msg("Failed to read named parameters.");
        } else {
            return false;
        }

        return true;
    }

}
