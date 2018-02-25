package com.tommytony.war.command.zonemaker;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import com.tommytony.war.structure.Cake;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;

/**
 * Deletes a cake.
 *
 * @author tommytony
 */
public class DeleteCakeCommand extends AbstractZoneMakerCommand {

    public DeleteCakeCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        Warzone zone;

        if (this.args.length == 2) {
            zone = Warzone.getZoneByName(this.args[0]);
            this.args[0] = this.args[1];
        } else if (this.args.length == 1) {
            zone = getWarzoneByLocation();
        } else {
            return false;
        }

        if (zone == null) {
            return false;
        } else if (!this.isSenderAuthorOfZone(zone)) {
            return true;
        }

        Cake cake = zone.getCake(this.args[0]);
        if (cake != null) {
            cake.getVolume().resetBlocks();
            zone.getCakes().remove(cake);
            WarzoneYmlMapper.save(zone);
            this.msg("Cake " + cake.getName() + " removed.");
            War.war.log(this.getSender().getName() + " deleted cake " + cake.getName() + " in warzone " + zone.getName(), Level.INFO);
        } else {
            this.badMsg("No such cake.");
        }

        return true;
    }
}
