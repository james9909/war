package com.tommytony.war.command.admin;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.VolumeMapper;
import com.tommytony.war.mapper.WarYmlMapper;
import com.tommytony.war.structure.WarHub;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;

/**
 * Deletes the warhub.
 *
 * @author Tim Düsterhus
 */
public class DeleteWarhubCommand extends AbstractWarAdminCommand {

    public DeleteWarhubCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotWarAdminException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (this.args.length != 0) {
            return false;
        }

        if (War.war.getWarHub() != null) {
            // reset existing hub
            War.war.getWarHub().getVolume().resetBlocks();
            VolumeMapper.delete(War.war.getWarHub().getVolume());
            War.war.setWarHub((WarHub) null);

            this.msg("War hub removed.");
            War.war.log(this.getSender().getName() + " deleted warhub", Level.INFO);
        } else {
            this.badMsg("No War hub to delete.");
        }
        WarYmlMapper.save();

        return true;
    }
}
