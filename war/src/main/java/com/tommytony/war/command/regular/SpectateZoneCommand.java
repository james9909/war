package com.tommytony.war.command.regular;

import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.config.WarzoneConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateZoneCommand extends AbstractWarCommand {
    public SpectateZoneCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        CommandSender sender = this.getSender();
        if (!(sender instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }
        Player player = (Player) sender;

        Warzone zone;
        if (this.args.length == 0) {
            zone = getWarzoneByLocation();
        } else {
            zone = Warzone.getZoneByName(args[0]);
        }
        if (zone == null) {
            this.badMsg("That's not a valid warzone");
            return true;
        }

        if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
            this.badMsg("join.disabled");
        } else if (zone.isReinitializing()) {
            this.badMsg("join.disabled");
        } else {
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            if (warPlayer.isSpectating()) {
                this.badMsg("You are already spectating!");
                return true;
            }
            if (warPlayer.getZone() != null) {
                this.badMsg("You are already playing!");
                return true;
            }
            zone.addSpectator(warPlayer);
        }

        return true;
    }
}
