package com.tommytony.war.job;

import com.tommytony.war.War;
import com.tommytony.war.utility.WarScoreboard;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateScoreboardJob extends BukkitRunnable {

    @Override
    public void run() {
        if (!War.war.isLoaded()) {
            return;
        }
        for (WarScoreboard scoreboard : WarScoreboard.getScoreboards().values()) {
            scoreboard.update();
        }
    }
}
