package com.tommytony.war.command.regular;

import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;

/**
 * Shows team information
 *
 * @author Tim Düsterhus
 */
public class TeamsCommand extends AbstractWarCommand {

    public TeamsCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        Warzone zone;
        if (this.args.length == 0) {
            zone = getWarzoneByLocation();
        } else if (this.args.length == 1) {
            zone = Warzone.getZoneByName(this.args[0]);
        } else {
            return false;
        }
        if (zone == null) {
            return false;
        }

        this.msg(zone.getTeamInformation());

        return true;
    }
}
