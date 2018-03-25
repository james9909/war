package com.tommytony.war.command.zonemaker;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.runnable.PartialZoneResetJob;
import java.util.Iterator;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;


public class ResetZoneCommand extends AbstractZoneMakerCommand {

    public ResetZoneCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    public static void forceResetZone(Warzone zone, CommandSender sender) {
        zone.clearThieves();
        for (Team team : zone.getTeams()) {
            team.teamcast("The war has ended. " + zone.getTeamInformation() + " Resetting warzone " + zone.getName() + " and teams...");
            for (Iterator<WarPlayer> it = team.getPlayers().iterator(); it.hasNext(); ) {
                WarPlayer warPlayer = it.next();
                it.remove();
                team.removePlayer(warPlayer);
            }
            team.resetPoints();
            team.getPlayers().clear();
        }

        War.war.msg(sender, "Reloading warzone " + zone.getName() + ".");

        PartialZoneResetJob.setSenderToNotify(zone, sender);

        zone.reinitialize();

        War.war.log(sender.getName() + " reset warzone " + zone.getName(), Level.INFO);
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
        } else if (!this.isSenderAuthorOfZone(zone)) {
            return true;
        }

        forceResetZone(zone, this.getSender());

        return true;
    }
}
