package com.tommytony.war.mapper;

import com.tommytony.war.Warzone;
import com.tommytony.war.structure.ZonePortal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ZonePortalYmlMapper {

    public static void fromZonePortalsToConfig(ConfigurationSection config, List<ZonePortal> portals) {
        for (ZonePortal portal : portals) {
            fromZonePortalToConfig(config, portal);
        }
    }

    public static void fromZonePortalToConfig(ConfigurationSection config, ZonePortal portal) {
        if (config == null) {
            return;
        }
        ConfigurationSection section = config.createSection(portal.getName());
        section.set("location", locationToString(portal.getLocation()));
    }

    public static List<ZonePortal> fromConfigToZonePortals(ConfigurationSection config, Warzone zone) {
        if (config == null) {
            return new ArrayList<>();
        }
        List<ZonePortal> portals = new ArrayList<>();
        Set<String> portalNames = config.getKeys(false);
        for (String portalName : portalNames) {
            portals.add(fromConfigToZonePortal(config, zone, portalName));
        }
        return portals;
    }

    public static ZonePortal fromConfigToZonePortal(ConfigurationSection config, Warzone zone, String portalName) {
        if (config == null) {
            return null;
        }

        String locationString = config.getString(portalName + ".location");
        Location location = stringToLocation(locationString);
        return new ZonePortal(portalName, zone, location);
    }

    public static String locationToString(Location location) {
        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        return String.format("%s:%d,%d,%d,%f,%f", world, x, y, z, yaw, pitch);
    }

    public static Location stringToLocation(String locationString) {
        String[] split = locationString.split(":");
        World world = Bukkit.getWorld(split[0]);
        String[] coords = split[1].split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        int z = Integer.parseInt(coords[2]);
        float yaw = Float.parseFloat(coords[3]);
        float pitch = Float.parseFloat(coords[4]);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
