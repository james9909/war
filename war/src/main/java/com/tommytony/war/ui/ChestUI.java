package com.tommytony.war.ui;

import com.tommytony.war.War;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Connor on 7/25/2017.
 */
public abstract class ChestUI {

    private Map<ItemStack, Runnable> actions;

    ChestUI() {
        actions = new HashMap<>();
    }

    protected void addItem(Inventory inv, int slot, ItemStack item, Runnable action) {
        inv.setItem(slot, item);
        actions.put(item, action);
    }

    protected ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    protected ItemStack createSaveItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Save");
        item.setItemMeta(meta);
        return item;
    }

    protected ItemStack createDeleteItem() {
        ItemStack item = new ItemStack(Material.TNT, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Delete");
        item.setItemMeta(meta);
        return item;
    }

    public abstract void build(Player player, Inventory inv);

    public abstract String getTitle();

    public abstract int getSize();

    boolean processClick(ItemStack clicked, Inventory inventory) {
        if (actions.containsKey(clicked)) {
            War.war.getServer().getScheduler().runTask(War.war, actions.get(clicked));
            return true;
        }
        return false;
    }
}
