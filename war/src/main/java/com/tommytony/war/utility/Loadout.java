package com.tommytony.war.utility;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Represents a loadout of items
 *
 * @author cmastudios
 */
public class Loadout implements Comparable<Loadout> {

    private String name;

    private ItemStack[] items;
    private ItemStack offhand;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

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
        this(name, loadoutChest.getBlockInventory());
    }

    public Loadout(String name, Inventory inventory) {
        this.name = name;
        setItemsFromInventory(inventory);
    }

    public Loadout(String name, ItemStack[] items) {
        this.name = name;
        this.items = items;
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

    public void setItemsFromInventory(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        setItemsFromItemList(contents);
    }

    public void setArmor(ItemStack[] armor) {
        helmet = armor[0];
        chestplate = armor[1];
        leggings = armor[2];
        boots = armor[3];
    }

    public void setItemsFromItemList(ItemStack[] itemsList) {
        int length = itemsList.length;
        items = Arrays.copyOfRange(itemsList, 0, length-5);
        offhand = itemsList[length-5];

        ItemStack[] armor = Arrays.copyOfRange(itemsList, length-4, length);
        setArmor(armor);
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

        if (offhand != null) {
            inventory.setItemInOffHand(offhand);
        }
        if (helmet != null) {
            inventory.setHelmet(helmet);
        }
        if (chestplate != null) {
            inventory.setChestplate(chestplate);
        }
        if (leggings != null) {
            inventory.setLeggings(leggings);
        }
        if (boots != null) {
            inventory.setBoots(boots);
        }
    }

    public ItemStack[] getItems() {
        return items;
    }

    public void setItems(ItemStack[] items) {
        this.items = items;
    }

    public ItemStack getOffhand() {
        return offhand;
    }

    public void setOffhand(ItemStack offhand) {
        this.offhand = offhand;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }
}
