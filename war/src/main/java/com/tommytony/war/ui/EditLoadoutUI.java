package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfigBag;
import com.tommytony.war.config.WarzoneConfigBag;
import com.tommytony.war.utility.Loadout;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Connor on 7/29/2017.
 */
class EditLoadoutUI extends ChestUI {

    private final Loadout loadout;
    private final Warzone zone;
    private final Team team;

    EditLoadoutUI(Loadout ldt, Warzone zone, Team team) {
        super();
        this.loadout = ldt;
        this.zone = zone;
        this.team = team;
    }

    @Override
    public void build(final Player player, final Inventory inv) {
        HashMap<Integer, ItemStack> lc = loadout.getContents();
        for (Integer slot : lc.keySet()) {
            ItemStack item = lc.get(slot);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            switch (slot) {
                case 100:
                    inv.setItem(9 * 4, item.clone());
                    break;
                case 101:
                    inv.setItem(9 * 4 + 1, item.clone());
                    break;
                case 102:
                    inv.setItem(9 * 4 + 2, item.clone());
                    break;
                case 103:
                    inv.setItem(9 * 4 + 3, item.clone());
                    break;
                default:
                    inv.setItem(slot, item.clone());
                    break;
            }
        }
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Save");
        item.setItemMeta(meta);
        this.addItem(inv, getSize() - 2, item, () -> {
            HashMap<Integer, ItemStack> nc = new HashMap<>();
            for (int i = 0; i < 9 * 4 + 4; i++) {
                int slot = i;
                if (i >= 9 * 4) {
                    slot = i + 64;
                }
                ItemStack item1 = inv.getItem(i);
                if (item1 != null && item1.getType() != Material.AIR) {
                    nc.put(slot, item1);
                }
            }
            loadout.setContents(nc);
            if (zone != null) {
                WarzoneConfigBag.afterUpdate(zone, player, "loadout updated", false);
            } else if (team != null) {
                TeamConfigBag.afterUpdate(team, player, "loadout updated", false);
            }
        });
        item = new ItemStack(Material.TNT);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Delete");
        item.setItemMeta(meta);
        this.addItem(inv, getSize() - 1, item, () -> {
            if (zone != null) {
                zone.getDefaultInventories().removeLoadout(loadout.getName());
                WarzoneConfigBag.afterUpdate(zone, player, "loadout deleted", false);
            } else if (team != null) {
                team.getInventories().removeLoadout(loadout.getName());
                TeamConfigBag.afterUpdate(team, player, "loadout deleted", false);
            }
        });
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Updating loadout " + loadout.getName();
    }

    @Override
    public int getSize() {
        return 9 * 5;
    }
}
