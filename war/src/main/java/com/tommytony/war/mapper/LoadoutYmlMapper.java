package com.tommytony.war.mapper;

import com.tommytony.war.War;
import com.tommytony.war.utility.Loadout;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LoadoutYmlMapper {

    /**
     * Deserializes loadouts from the configuration. Backwards compatibility: returns new-style loadouts and still modifies the loadouts parameter.
     *
     * @param config A configuration section that contains loadouts
     * @return list of new style loadouts
     */
    public static Map<String, Loadout> fromConfigToLoadouts(ConfigurationSection config) {
        if (config == null) {
            return new TreeMap<>();
        }
        Set<String> loadoutNames = config.getKeys(false);
        Map<String, Loadout> loadouts = new TreeMap<>();
        for (String name : loadoutNames) {
            Loadout loadout = fromConfigToLoadout(config, name);
            if (loadout != null) {
                loadouts.put(name, loadout);
                War.war.getLogger().info("Loaded class " + loadout.getName());
            } else {
                War.war.getLogger().warning("Failed to load class" + name);
            }
        }
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
        if (config == null) {
            return null;
        }
        ConfigurationSection section = config.getConfigurationSection(loadoutName);
        if (section == null) {
            return null;
        }
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection == null) {
            return null;
        }
        Set<String> slots = itemsSection.getKeys(false);

        HashMap<Integer, ItemStack> map = new HashMap<>();
        for (String slot : slots) {
            ItemStack item = itemsSection.getItemStack(slot);
            map.put(Integer.parseInt(slot), item);
        }

        ItemStack helmet = (ItemStack) section.get("helmet");
        ItemStack chestplate = (ItemStack) section.get("chestplate");
        ItemStack leggings = (ItemStack) section.get("leggings");
        ItemStack boots = (ItemStack) section.get("boots");
        ItemStack offhand = (ItemStack) section.get("offhand");

        ItemStack[] items = mapToItems(map);

        Loadout loadout = new Loadout(loadoutName, items);
        loadout.setArmor(new ItemStack[]{helmet, chestplate, leggings, boots});
        loadout.setOffhand(offhand);
        loadout.setDefault(section.getBoolean("default"));

        return loadout;
    }

    /**
     * Serializes a list of new style loadouts to the configuration.
     *
     * @param loadouts List of new style loadouts
     * @param section Section of the configuration to write to
     */
    public static void fromLoadoutsToConfig(Map<String, Loadout> loadouts, ConfigurationSection section) {
        for (Loadout ldt : loadouts.values()) {
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
        if (loadout != null) {
            ConfigurationSection loadoutSection = section.createSection(loadout.getName());

            ConfigurationSection itemsSection = loadoutSection.createSection("items");
            HashMap<Integer, ItemStack> map = itemsToMap(loadout.getItems());
            for (Integer slot : map.keySet()) {
                itemsSection.set(slot.toString(), map.get(slot));
            }

            loadoutSection.set("helmet", loadout.getHelmet());
            loadoutSection.set("chestplate", loadout.getChestplate());
            loadoutSection.set("leggings", loadout.getLeggings());
            loadoutSection.set("boots", loadout.getBoots());
            loadoutSection.set("offhand", loadout.getOffhand());
            loadoutSection.set("default", loadout.getDefault());
        }
    }

    public static HashMap<Integer, ItemStack> itemsToMap(ItemStack[] items) {
        HashMap<Integer, ItemStack> map = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                map.put(i, items[i]);
            }
        }
        return map;
    }

    public static ItemStack[] mapToItems(HashMap<Integer, ItemStack> map) {
        ItemStack[] items = new ItemStack[9*4];
        for (Integer key : map.keySet()) {
            items[key] = map.get(key);
        }
        return items;
    }
}
