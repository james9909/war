package com.tommytony.war.command.regular;

import com.tommytony.war.Team;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.command.WarCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sends a message to all team-members
 *
 * @author Tim Düsterhus
 */
public class TeamCommand extends AbstractWarCommand {

    public TeamCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (!(this.getSender() instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }

        Player player = (Player) this.getSender();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team team = warPlayer.getTeam();
        if (team == null) {
            return false;
        }

        if (this.args.length < 1) {
            if (team.isInTeamChat(warPlayer)) {
                team.removeTeamChatPlayer(warPlayer);
                this.msg("team.chat.disable");
            } else {
                team.addTeamChatPlayer(warPlayer);
                this.msg("team.chat.enable");
            }
            return true;
        }

        StringBuilder teamMessage = new StringBuilder();
        for (String part : this.args) {
            teamMessage.append(part).append(' ');
        }
        team.sendTeamChatMessage(player, teamMessage.toString());
        return true;
    }
}
