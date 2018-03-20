package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.utility.Loadout;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Create new loadout");
        item.setItemMeta(meta);
        this.addItem(inv, i++, item, () -> { War.war.getUIManager().assignUI(player, new CreateLoadoutUI(zone, team)); });

        Map<String, Loadout> loadouts = getLoadouts();

        for (Loadout loadout : loadouts.values()) {
            item = new ItemStack(Material.CHEST);
            meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + String.format("Edit loadout %s", loadout.getName()));
            item.setItemMeta(meta);
            this.addItem(inv, i++, item, () -> { War.war.getUIManager().assignUI(player, new EditLoadoutUI(zone, team, loadout)); });
        }
    }

    @Override
    public String getTitle() {
        if (zone != null) {
            return ChatColor.RED + String.format("Edit loadouts for %s", zone.getName());
        } else if (team != null) {
            return ChatColor.RED + String.format("Edit loadouts for %s", team.getName());
        } else {
            return ChatColor.RED + "Edit default loadouts";
        }
    }

    public Map<String, Loadout> getLoadouts() {
        if (zone != null) {
            return zone.getDefaultInventories().getLoadouts();
        } else if (team != null) {
            return team.getInventories().getLoadouts();
        } else {
            return War.war.getDefaultInventories().getLoadouts();
        }
    }

    @Override
    public int getSize() {
        Map<String, Loadout> loadouts = getLoadouts();
        int size = loadouts.size() + 1;
        if (size % 9 == 0) {
            return size;
        }
        return (size / 9 + 1) * 9;
    }
}
