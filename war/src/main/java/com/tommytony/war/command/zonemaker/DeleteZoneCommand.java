package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.WarYmlMapper;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;

/**
 * Deletes a warzone.
 *
 * @author Tim Düsterhus
 */
public class DeleteZoneCommand extends AbstractZoneMakerCommand {

    public DeleteZoneCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    public static void forceDeleteZone(Warzone zone, CommandSender sender) {
        War.war.getWarzones().remove(zone);
        WarYmlMapper.save();

        WarzoneYmlMapper.delete(zone);

        String msg = "Warzone " + zone.getName() + " removed by " + sender.getName() + ".";
        War.war.log(msg, Level.INFO);
        War.war.msg(sender, msg);
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

        forceDeleteZone(zone, getSender());

        return true;
    }
}
