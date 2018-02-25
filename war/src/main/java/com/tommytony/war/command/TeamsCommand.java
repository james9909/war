package com.tommytony.war.command;

import com.tommytony.war.Warzone;
import com.tommytony.war.structure.ZoneLobby;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (this.args.length == 1) {
            zone = Warzone.getZoneByName(this.args[0]);
        } else if (this.args.length == 0) {
            zone = getWarzoneByLocation();
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
