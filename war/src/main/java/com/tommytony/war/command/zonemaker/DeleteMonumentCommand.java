package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import com.tommytony.war.structure.Monument;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;

/**
 * Deletes a monument.
 *
 * @author Tim Düsterhus
 */
public class DeleteMonumentCommand extends AbstractZoneMakerCommand {

    public DeleteMonumentCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
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

        Monument monument = zone.getMonument(this.args[0]);
        if (monument != null) {
            monument.getVolume().resetBlocks();
            zone.getMonuments().remove(monument);
            WarzoneYmlMapper.save(zone);
            this.msg("Monument " + monument.getName() + " removed.");
            War.war.log(this.getSender().getName() + " deleted monument " + monument.getName() + " in warzone " + zone.getName(), Level.INFO);
        } else {
            this.badMsg("No such monument.");
        }

        return true;
    }
}
