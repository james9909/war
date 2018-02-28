package com.tommytony.war.command.regular;

import com.tommytony.war.Warzone;
import com.tommytony.war.Warzone.LeaveCause;
import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Leaves a game.
 *
 * @author Tim Düsterhus
 */
public class LeaveCommand extends AbstractWarCommand {

    public LeaveCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (!(this.getSender() instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }

        if (this.args.length != 0) {
            return false;
        }

        Player player = (Player) this.getSender();
        Warzone zone = Warzone.getZoneByPlayerName(player.getName());
        if (zone == null) {
            return false;
        }

        zone.handlePlayerLeave(player, true);
        return true;
    }
}
