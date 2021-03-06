package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import com.tommytony.war.structure.Bomb;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

/**
 * Deletes a bomb.
 *
 * @author tommytony
 */
public class DeleteBombCommand extends AbstractZoneMakerCommand {

    public DeleteBombCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
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

        Bomb bomb = zone.getBomb(this.args[0]);
        if (bomb != null) {
            bomb.getVolume().resetBlocks();
            zone.getBombs().remove(bomb);
            WarzoneYmlMapper.save(zone);
            this.msg("Bomb " + bomb.getName() + " removed.");
            War.war.log(this.getSender().getName() + " deleted bomb " + bomb.getName() + " in warzone " + zone.getName(), Level.INFO);
        } else {
            this.badMsg("No such bomb.");
        }

        return true;
    }
}
