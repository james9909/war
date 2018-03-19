package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfigBag;
import com.tommytony.war.config.WarConfigBag;
import com.tommytony.war.config.WarzoneConfigBag;
import com.tommytony.war.utility.Loadout;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EditLoadoutUI extends ChestUI {

    private Warzone zone;
    private Team team;
    private Loadout loadout;

    EditLoadoutUI(Warzone zone, Team team, Loadout loadout) {
        this.zone = zone;
        this.team = team;
        this.loadout = loadout;
    }

    @Override
    public void build(Player player, Inventory inv) {
        ItemStack item;
        ItemMeta meta;

        for (ItemStack loadoutItem : loadout.getItems()) {
            if (loadoutItem != null) {
                inv.addItem(loadoutItem);
            }
        }

        this.addItem(inv, 9*4, loadout.getOffhand(), null);
        this.addItem(inv, 9*4+1, loadout.getHelmet(), null);
        this.addItem(inv, 9*4+2, loadout.getChestplate(), null);
        this.addItem(inv, 9*4+3, loadout.getLeggings(), null);
        this.addItem(inv, 9*4+4, loadout.getBoots(), null);

        item = new ItemStack(Material.NETHER_STAR, 1);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Save");
        item.setItemMeta(meta);
        this.addItem(inv, getSize() - 2, item, () -> {
            ItemStack[] contents = inv.getContents();
            contents = Arrays.copyOfRange(contents, 0, 9*4+5);

            loadout.setItemsFromItemList(contents);

            if (zone != null) {
                WarzoneConfigBag.afterUpdate(zone, player, "Loadout updated", false);
            } else if (team != null) {
                TeamConfigBag.afterUpdate(team, player, "Loadout updated", false);
            } else {
                WarConfigBag.afterUpdate(player, "Loadout updated", false);
            }
        });

        item = new ItemStack(Material.TNT, 1);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Delete");
        item.setItemMeta(meta);
        this.addItem(inv, getSize() - 1, item, () -> {
            if (zone != null) {
                zone.getDefaultInventories().getLoadouts().remove(loadout);
                WarzoneConfigBag.afterUpdate(zone, player, "Loadout deleted", false);
            } else if (team != null) {
                team.getInventories().getLoadouts().remove(loadout);
                TeamConfigBag.afterUpdate(team, player, "Loadout deleted", false);
            } else {
                War.war.getDefaultInventories().getLoadouts().remove(loadout);
                WarConfigBag.afterUpdate(player, "Loadout deleted", false);
            }
        });
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + String.format("Edit loadout %s", loadout.getName());
    }

    @Override
    public int getSize() {
        return 9*5;
    }
}
