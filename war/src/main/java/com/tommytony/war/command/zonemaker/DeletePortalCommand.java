package com.tommytony.war.command.zonemaker;

import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeletePortalCommand extends AbstractZoneMakerCommand {

    public DeletePortalCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        CommandSender sender = this.getSender();
        if (!(sender instanceof Player)) {
            this.badMsg("You can't do this if you are not in-game.");
            return true;
        }

        if (args.length != 2) {
            return false;
        }

        String zoneName = args[0];
        String portalName = args[1];

        Warzone zone = Warzone.getZoneByName(zoneName);
        if (zone == null) {
            this.badMsg("zone.zone404");
            return true;
        }

        if (zone.deletePortal(portalName)) {
            this.msg("Portal {0} for {1} has been deleted", portalName, zoneName);
        } else {
            this.badMsg("Portal not found");
        }

        return true;
    }
}
