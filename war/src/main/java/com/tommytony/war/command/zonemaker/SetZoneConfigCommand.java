package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.config.bags.WarzoneConfigBag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetZoneConfigCommand extends AbstractOptionalZoneMakerCommand {

    public SetZoneConfigCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args, false);
    }

    @Override
    public boolean handle() {
        Warzone zone = null;
        Player player = null;
        CommandSender commandSender = this.getSender();
        boolean isFirstParamWarzone = false;
        boolean wantsToPrint = false;

        if (this.args.length == 0) {
            return false;
        }

        if (!this.args[0].contains(":")) {
            // warzone name maybe in first place
            Warzone zoneByName = Warzone.getZoneByName(this.args[0]);
            if (zoneByName != null) {
                zone = zoneByName;
                isFirstParamWarzone = true;
            } else if (this.args[0].equals("-p") || this.args[0].equals("print")) {
                wantsToPrint = true;
            }
        }

        if (this.getSender() instanceof Player) {
            player = (Player) commandSender;
            if (zone == null) {
                // zone not found, is he standing in it?
                zone = getWarzoneByLocation();
            }
        }

        if (zone == null) {
            // No warzone found, whatever the mean, escape
            return false;
        }

        if (isFirstParamWarzone) {
            if (this.args.length == 1) {
                // Only one param: the warzone name - default to usage
                return false;
            }
            // More than one param: the arguments need to be shifted
            String[] newargs = new String[this.args.length - 1];
            for (int i = 1; i < this.args.length; i++) {
                newargs[i - 1] = this.args[i];
            }
            this.args = newargs;
        }

        // args have been shifted if needed
        if (this.args.length > 0 && (this.args[0].equals("-p") || this.args[0].equals("print"))) {
            // only printing
            if (this.args.length == 1) {
                this.msg(War.war.printConfig(zone));
                return true;
            } else {
                // first param was to print, shift again
                String[] newargs = new String[this.args.length - 1];
                for (int i = 1; i < this.args.length; i++) {
                    newargs[i - 1] = this.args[i];
                }
                this.args = newargs;
            }
            wantsToPrint = true;
        }

        if (!this.isSenderZoneMaker()) {
            War.war.badMsg(this.getSender(), "You can't do this if you are not a warzone maker (permission war.zonemaker).");
            return true;
        } else if (!this.isSenderAuthorOfZone(zone)) {
            return true;
        }

        // We have a warzone and indexed-from-0 arguments, let's update
        String namedParamReturn = War.war.updateZoneFromNamedParams(zone, player, this.args);
        if (!namedParamReturn.equals("") && !namedParamReturn.equals("PARSE-ERROR")) {
            WarzoneConfigBag.afterUpdate(zone, player, namedParamReturn, wantsToPrint);
        } else if (namedParamReturn.equals("PARSE-ERROR")) {
            this.badMsg("Failed to read named parameter(s).");
        } else {
            // empty return means no param was parsed - print command usage
            return false;
        }

        return true;
    }
}
