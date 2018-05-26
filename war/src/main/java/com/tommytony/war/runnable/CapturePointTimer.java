package com.tommytony.war.runnable;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.structure.CapturePoint;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CapturePointTimer extends BukkitRunnable {

    private Warzone zone;

    public CapturePointTimer(Warzone zone) {
        this.zone = zone;
    }

    @Override
    public void run() {
        if (!War.war.isLoaded()) {
            return;
        }
        if (zone.getTeams().size() > 2) {
            return;
        }
        boolean active = true;
        for (Team team : zone.getTeams()) {
            if (team.getPlayers().size() == 0) {
                active = false;
                break;
            }
        }

        Set<WarPlayer> players = zone.getPlayers();
        for (CapturePoint cp : zone.getCapturePoints()) {
            cp.clearActiveTeams();

            for (Iterator<WarPlayer> it = players.iterator(); it.hasNext();) {
                WarPlayer warPlayer = it.next();
                Player player = warPlayer.getPlayer();
                Team team = warPlayer.getTeam();

                Block standing = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                boolean valid = standing.getType().equals(Material.WOOL) || standing.getType().equals(Material.DOUBLE_STEP);
                if (valid && cp.getVolume().contains(standing)) {
                    cp.addActiveTeam(team.getKind());
                    it.remove();
                }
            }
            cp.calculateStrength(active);

            if (cp.getController() != null && cp.getController() != cp.getDefaultController() && cp.getStrength() == cp.getMaxStrength()) {
                int controlTime = cp.getControlTime() + 1;
                cp.setControlTime(controlTime);
                if (controlTime % cp.getMaxStrength() == 0) {
                    // give points for every control time which is a multiple of the time taken to capture
                    Team team = zone.getTeamByKind(cp.getController());
                    team.addPoint();
                    zone.broadcast("zone.capturepoint.addpoint", cp.getController().getFormattedName(), cp.getName());
                    // Detect win conditions
                    if (team.getPoints() >= team.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)) {
                        zone.handleScoreCapReached(team.getName());
                    } else {
                        // just added a point
                        team.resetSign();
                    }
                }
            }
        }
    }
}
