package com.tommytony.war.command.zonemaker;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

/**
 * Deletes a monument.
 *
 * @author Tim Düsterhus
 */
public class DeleteTeamFlagCommand extends AbstractZoneMakerCommand {

    public DeleteTeamFlagCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        Warzone zone;

        if (this.args.length == 1) {
            zone = getWarzoneByLocation();
        } else if (this.args.length == 2) {
            zone = Warzone.getZoneByName(this.args[0]);
            this.args[0] = this.args[1];
        } else {
            return false;
        }

        if (zone == null) {
            return false;
        } else if (!this.isSenderAuthorOfZone(zone)) {
            return true;
        }

        Team teamFlagTeam = null;
        for (Team team : zone.getTeams()) {
            if (team.getName().startsWith(this.args[0].toLowerCase())) {
                teamFlagTeam = team;
            }
        }
        if (teamFlagTeam != null) {
            teamFlagTeam.deleteTeamFlag();

            WarzoneYmlMapper.save(zone);
            this.msg(teamFlagTeam.getName() + " flag removed.");
            War.war.log(this.getSender().getName() + " deleted team " + teamFlagTeam.getName() + " flag in warzone " + zone.getName(), Level.INFO);
        } else {
            this.badMsg("No such team flag.");
        }

        return true;
    }
}
