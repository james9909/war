package com.tommytony.war.command.zonemaker;

import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.mapper.WarzoneYmlMapper;
import com.tommytony.war.structure.ZonePortal;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetZonePortalCommand extends AbstractZoneMakerCommand {

    public SetZonePortalCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        CommandSender sender = this.getSender();
        if (!(sender instanceof Player)) {
            this.badMsg("You can't do this if you are not in-game.");
            return true;
        }

        if (this.args.length != 2) {
            return false;
        }

        Player player = (Player) sender;

        String zoneName = args[0];
        String portalName = args[1];
        Warzone zone = Warzone.getZoneByName(zoneName);
        if (zone == null) {
            this.badMsg("zone.zone404");
            return true;
        }

        Location playerLocation = player.getLocation();
        ZonePortal portal = new ZonePortal(portalName, zone, playerLocation);

        zone.addPortal(portal);
        WarzoneYmlMapper.save(zone);
        this.msg("Portal {0} set for zone {1}", portalName, zone.getName());
        return true;
    }
}
