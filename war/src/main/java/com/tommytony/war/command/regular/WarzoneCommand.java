package com.tommytony.war.command.regular;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.config.WarConfig;
import com.tommytony.war.job.TeleportPlayerJob;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Warps the player to the given warzone.
 *
 * @author Tim Düsterhus
 */
public class WarzoneCommand extends AbstractWarCommand {

    public WarzoneCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (!(this.getSender() instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }
        Player player = (Player) this.getSender();
        if (args.length != 1) {
            return false;
        }
        if (!War.war.canWarp(player)) {
            this.badMsg("zone.warp.permission");
        }
        Warzone warzone = Warzone.getZoneByName(args[0]);
        if (warzone != null && warzone.getTeleport() != null) {
            Warzone playerWarzone = Warzone.getZoneByPlayerName(player.getName());
            int warmup = War.war.getWarConfig().getInt(WarConfig.TPWARMUP);
            if (playerWarzone != null) {
                playerWarzone.handlePlayerLeave(player, warzone.getTeleport(), true);
            }
            if (warmup > 0 && !player.hasPermission("war.warmupexempt")) {
                final int TICKS_PER_SECOND = 20;
                TeleportPlayerJob job = new TeleportPlayerJob(player, warzone.getTeleport());
                job.runTaskLater(War.war, warmup);
                this.msg("command.tp.init", warmup / TICKS_PER_SECOND);
            } else {
                player.teleport(warzone.getTeleport());
            }
        } else {
            this.badMsg("zone.zone404");
        }
        return true;
    }
}
