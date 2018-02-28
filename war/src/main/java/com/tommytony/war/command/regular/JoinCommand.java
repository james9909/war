package com.tommytony.war.command.regular;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.config.TeamKind;
import com.tommytony.war.config.WarzoneConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Joins a team.
 *
 * @author Tim Düsterhus
 */
public class JoinCommand extends AbstractWarCommand {

    public JoinCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (!(this.getSender() instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }

        Player player = (Player) this.getSender();

        Warzone zone;
        if (this.args.length == 0) {
            zone = getWarzoneByLocation();
        } else {
            zone = Warzone.getZoneByName(args[0]);
        }
        if (zone == null) {
            return false;
        }
        if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
            this.badMsg("join.disabled");
        } else if (zone.isReinitializing()) {
            this.badMsg("join.disabled");
        } else if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.JOINMIDBATTLE) && zone.isEnoughPlayers()) {
            this.badMsg("join.progress");
        } else {
            zone.autoAssign(player);
        }
        return true;
    }
}
