package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.job.PartialZoneResetJob;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;


public class NextBattleCommand extends AbstractZoneMakerCommand {

    public NextBattleCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
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

        zone.clearThieves();
        zone.broadcast("zone.battle.next", zone.getName());

        PartialZoneResetJob.setSenderToNotify(zone, this.getSender());

        zone.reinitialize();

        War.war.log(this.getSender().getName() + " used nextbattle in warzone " + zone.getName(), Level.INFO);

        return true;
    }
}
