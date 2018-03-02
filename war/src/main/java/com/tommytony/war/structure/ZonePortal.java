package com.tommytony.war.structure;

import com.tommytony.war.Warzone;
import com.tommytony.war.volume.Volume;
import org.bukkit.Location;

public class ZonePortal {

    private Warzone zone;
    private Volume volume;
    private Location location;


    public ZonePortal(Warzone zone, Location location) {
        this.zone = zone;
        this.location = location;
    }
}
