package com.tommytony.war.runnable;

import com.tommytony.war.Warzone;
import org.bukkit.entity.Player;

public class InitZoneJob implements Runnable {

    private final Warzone zone;
    private final Player respawnExempted;

    public InitZoneJob(Warzone zone) {
        this.zone = zone;
        this.respawnExempted = null;
    }

    public InitZoneJob(Warzone warzone, Player respawnExempted) {
        this.zone = warzone;
        this.respawnExempted = respawnExempted;
    }

    public void run() {
        this.zone.initializeZone(this.respawnExempted);
    }

}
