package com.tommytony.war.job;

import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;

public class LoadoutResetJob implements Runnable {

    private final WarPlayer warPlayer;

    public LoadoutResetJob(WarPlayer warPlayer) {
        this.warPlayer = warPlayer;
    }

    public void run() {
        Warzone zone = warPlayer.getZone();
        if (zone != null) {
            zone.equipPlayerLoadoutSelection(warPlayer);
        }
    }
}
