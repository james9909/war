package com.tommytony.war.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Represents a loadout of items
 *
 * @author cmastudios
 */
public class Loadout implements Comparable<Loadout> {

    private String name;
    private Chest loadoutChest;

    private ItemStack[] items;
    private ItemStack[] armor;
    private ItemStack offhand;

    private static HashSet<Material> HELMETS = new HashSet<>();
    private static HashSet<Material> CHESTPLATES = new HashSet<>();
    private static HashSet<Material> LEGGINGS = new HashSet<>();
    private static HashSet<Material> BOOTS = new HashSet<>();

    static {
        HELMETS.add(Material.LEATHER_HELMET);
        HELMETS.add(Material.GOLD_HELMET);
        HELMETS.add(Material.CHAINMAIL_HELMET);
        HELMETS.add(Material.IRON_HELMET);
        HELMETS.add(Material.DIAMOND_HELMET);

        HELMETS.add(Material.WOOL);
        HELMETS.add(Material.PUMPKIN);
        HELMETS.add(Material.JACK_O_LANTERN);
        HELMETS.add(Material.SKULL_ITEM);

        CHESTPLATES.add(Material.LEATHER_CHESTPLATE);
        CHESTPLATES.add(Material.GOLD_CHESTPLATE);
        CHESTPLATES.add(Material.CHAINMAIL_CHESTPLATE);
        CHESTPLATES.add(Material.IRON_CHESTPLATE);
        CHESTPLATES.add(Material.DIAMOND_CHESTPLATE);
        CHESTPLATES.add(Material.ELYTRA);

        LEGGINGS.add(Material.LEATHER_LEGGINGS);
        LEGGINGS.add(Material.GOLD_LEGGINGS);
        LEGGINGS.add(Material.CHAINMAIL_LEGGINGS);
        LEGGINGS.add(Material.IRON_LEGGINGS);
        LEGGINGS.add(Material.DIAMOND_LEGGINGS);

        BOOTS.add(Material.LEATHER_BOOTS);
        BOOTS.add(Material.GOLD_BOOTS);
        BOOTS.add(Material.CHAINMAIL_BOOTS);
        BOOTS.add(Material.IRON_BOOTS);
        BOOTS.add(Material.DIAMOND_BOOTS);
    }

    public Loadout(String name, Chest loadoutChest) {
        this.name = name;
        this.loadoutChest = loadoutChest;
        getItemsFromChest();
    }

    public static Loadout getLoadout(List<Loadout> loadouts, String name) {
        for (Loadout ldt : loadouts) {
            if (ldt.getName().equals(name)) {
                return ldt;
            }
        }
        return null;
    }

    public int compareTo(Loadout other) {
        return getName().compareTo(other.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void getItemsFromChest() {
        if (loadoutChest != null) {
            ItemStack[] contents = loadoutChest.getInventory().getContents();
            int length = contents.length;
            items = Arrays.copyOfRange(contents, 0, length-5);
            offhand = contents[length-5];
            armor = Arrays.copyOfRange(contents, length-4, length);
        }
    }

    public void setLoadoutChest(Chest loadoutChest) {
        this.loadoutChest = loadoutChest;
        getItemsFromChest();
    }

    public Chest getLoadoutChest() {
        return loadoutChest;
    }

    public void giveItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) {
                    inventory.addItem(item);
                }
            }
        }

        if (armor != null) {
            for (ItemStack item : armor) {
                if (item == null) {
                    continue;
                }
                Material type = item.getType();
                if (HELMETS.contains(type)) {
                    inventory.setHelmet(item);
                } else if (CHESTPLATES.contains(type)) {
                    inventory.setChestplate(item);
                } else if (LEGGINGS.contains(type)) {
                    inventory.setLeggings(item);
                } else if (BOOTS.contains(type)) {
                    inventory.setBoots(item);
                }
            }
        }
        if (offhand != null) {
            inventory.setItemInOffHand(offhand);
        }
    }
}
