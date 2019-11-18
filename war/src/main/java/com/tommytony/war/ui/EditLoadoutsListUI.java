package com.tommytony.war.ui;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.bags.TeamConfigBag;
import com.tommytony.war.config.bags.WarConfigBag;
import com.tommytony.war.config.bags.WarzoneConfigBag;
import com.tommytony.war.utility.Loadout;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import java.util.Collections;
import java.util.Map;

public class EditLoadoutsListUI extends ChestUI {

    private Warzone zone;
    private Team team;

    EditLoadoutsListUI(Warzone zone, Team team) {
        this.zone = zone;
        this.team = team;
    }

    @Override
    public void build(Player player, Inventory inv) {
        int i = 0;
        if (zone == null && team == null) {
            // Admin editing loadouts
            ItemStack item = createItem(Material.CHEST, ChatColor.GREEN + "Create new loadout", null);
            this.addItem(inv, i++, item, () -> War.war.getUIManager().assignUI(player, new CreateLoadoutUI()));

            Map<String, Loadout> loadouts = War.war.getLoadouts();
            for (String loadoutName: loadouts.keySet()) {
                Loadout loadout = loadouts.get(loadoutName);
                String title = ChatColor.GREEN + String.format("Edit loadout %s", loadout.getName());
                item = createItem(Material.CHEST, title, null);
                this.addItem(inv, i++, item, () -> War.war.getUIManager().assignUI(player, new EditLoadoutUI(loadout)));
            }
        } else {
            // Select which loadouts can be enabled
            for (String loadoutName : War.war.getLoadouts().keySet()) {
                Loadout loadout = War.war.getLoadout(loadoutName);

                boolean enabled;
                if (team != null) {
                    enabled = team.getInventories().containsLoadout(loadoutName);
                } else {
                    enabled = zone.getDefaultInventories().containsLoadout(loadoutName);
                }
                ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE, 1);
                ItemMeta meta = item.getItemMeta();
                String name = "Enabled: " + (enabled ? ChatColor.GREEN + "true" : ChatColor.DARK_GRAY + "false");
                meta.setDisplayName(loadout.getName());
                meta.setLore(new ImmutableList.Builder<String>().add(name).build());
                item.setItemMeta(meta);
                this.addItem(inv, i++, item, () -> {
                    if (enabled) {
                        // Previously enabled, so remove it!
                        if (team != null) {
                            team.getInventories().removeLoadout(loadoutName);
                            TeamConfigBag.afterUpdate(team, player, "Loadout updated", false);
                        } else if (zone != null) {
                            zone.getDefaultInventories().removeLoadout(loadoutName);
                            WarzoneConfigBag.afterUpdate(zone, player, "Loadout updated", false);
                        }
                    } else {
                        if (team != null) {
                            team.getInventories().addLoadout(loadoutName);
                            TeamConfigBag.afterUpdate(team, player, "Loadout updated", false);
                        } else if (zone != null) {
                            zone.getDefaultInventories().addLoadout(loadoutName);
                            WarzoneConfigBag.afterUpdate(zone, player, "Loadout updated", false);
                        }
                    }
                    War.war.getUIManager().assignUI(player, new EditLoadoutsListUI(zone, team));
                });
            }
        }
    }

    @Override
    public String getTitle() {
        if (zone != null) {
            return ChatColor.RED + String.format("Edit loadouts for %s", zone.getName());
        } else if (team != null) {
            return ChatColor.RED + String.format("Edit loadouts for %s", team.getName());
        } else {
            return ChatColor.RED + "Edit loadouts";
        }
    }

    @Override
    public int getSize() {
        Map<String, Loadout> loadouts = War.war.getLoadouts();
        int size = loadouts.size() + 1;
        if (size % 9 == 0) {
            return size;
        }
        return (size / 9 + 1) * 9;
    }
}
