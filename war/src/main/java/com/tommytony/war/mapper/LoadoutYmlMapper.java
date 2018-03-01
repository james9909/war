package com.tommytony.war.mapper;

import com.tommytony.war.War;
import com.tommytony.war.utility.Loadout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;

public class LoadoutYmlMapper {

    /**
     * Deserializes loadouts from the configuration. Backwards compatibility: returns new-style loadouts and still modifies the loadouts parameter.
     *
     * @param config A configuration section that contains loadouts
     * @return list of new style loadouts
     */
    public static List<Loadout> fromConfigToLoadouts(ConfigurationSection config) {
        if (config == null) {
            return new ArrayList<>();
        }
        Set<String> loadoutNames = config.getKeys(false);
        List<Loadout> loadouts = new ArrayList<>();
        for (String name : loadoutNames) {
            Loadout loadout = fromConfigToLoadout(config, name);
            if (loadout == null) {
                War.war.getLogger().warning("Failed to load class " + name);
            } else {
                loadouts.add(loadout);
                War.war.getLogger().info("Loaded class " + loadout.getName());
            }
        }
        Collections.sort(loadouts);
        return loadouts;
    }

    /**
     * Deserialize a loadout from the configuration. Backwards compatibility: returns new-style loadout and still modifies the loadout parameter.
     *
     * @param config A configuration section that contains loadouts
     * @param loadoutName The name of the loadout
     * @return new style loadout
     */
    public static Loadout fromConfigToLoadout(ConfigurationSection config, String loadoutName) {
        String chestLocation = config.getString(loadoutName + ".chest");
        Location location = getLocationFromString(chestLocation);
        BlockState state = location.getBlock().getState();
        if (state instanceof Chest) {
            return new Loadout(loadoutName, (Chest) state);
        }
        return null;
    }

    /**
     * Serializes a list of new style loadouts to the configuration.
     *
     * @param loadouts List of new style loadouts
     * @param section Section of the configuration to write to
     */
    public static void fromLoadoutsToConfig(List<Loadout> loadouts, ConfigurationSection section) {
        Collections.sort(loadouts);
        List<String> names = new ArrayList<>();
        for (Loadout ldt : loadouts) {
            names.add(ldt.getName());
            LoadoutYmlMapper.fromLoadoutToConfig(ldt, section);
        }
    }

    /**
     * Serialize a new style loadout to the configuration
     *
     * @param loadout New style loadout
     * @param section Section of the configuration to write to
     */
    public static void fromLoadoutToConfig(Loadout loadout, ConfigurationSection section) {
        LoadoutYmlMapper.fromLoadoutToConfig(loadout.getName(), loadout.getLoadoutChest(), section);
    }

    public static void fromLoadoutToConfig(String loadoutName, Chest loadoutChest, ConfigurationSection section) {
        ConfigurationSection loadoutSection = section.createSection(loadoutName);

        if (loadoutSection != null) {
            Location location = loadoutChest.getLocation();
            loadoutSection.set("chest", getLocationString(location));
        }
    }

    public static String getLocationString(Location location) {
        return String.format("%s:%f,%f,%f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    public static Location getLocationFromString(String locationString) {
        String[] split = locationString.split(":");
        World world = Bukkit.getWorld(split[0]);
        String[] coords = split[1].split(",");

        double x = Double.parseDouble(coords[0]);
        double y = Double.parseDouble(coords[1]);
        double z = Double.parseDouble(coords[2]);

        return new Location(world, x, y, z);
    }
}
