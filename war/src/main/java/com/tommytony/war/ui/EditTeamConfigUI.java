package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfigBag;
import com.tommytony.war.config.WarzoneConfigBag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EditTeamConfigUI extends ChestUI {

    private Warzone zone;
    private Team team;

    EditTeamConfigUI(Warzone zone, Team team) {
        super();
        this.zone = zone;
        this.team = team;
    }

    @Override
    public void build(Player player, Inventory inv) {
        TeamConfigBag config;
        if (team != null) {
            config = team.getTeamConfig();
        } else if (zone != null) {
            config = zone.getTeamDefaultConfig();
        } else {
            config = War.war.getTeamDefaultConfig();
        }
        UIConfigHelper.addTeamConfigOptions(this, player, inv, config, team, zone, 0);

        if (zone != null || team != null) {
            ItemStack item;
            ItemMeta meta;
            item = new ItemStack(Material.SNOW_BALL);
            meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Restore Defaults");
            item.setItemMeta(meta);
            this.addItem(inv, getSize() - 1, item, () -> {
                config.reset();
                if (team != null) {
                    TeamConfigBag.afterUpdate(team, player, String.format("%s team options set to defaults in warzone %ss", team.getName(), zone.getName()), false);
                } else if (zone != null) {
                    WarzoneConfigBag.afterUpdate(zone, player, "All team options set to defaults in warzone " + zone.getName(), false);
                }
                War.war.getUIManager().assignUI(player, new EditTeamConfigUI(zone, team));
            });
        }
    }

    @Override
    public String getTitle() {
        if (zone != null) {
            if (team != null) {
                return ChatColor.RED + String.format("Warzone %s Team %s Config", zone.getName(), team.getName());
            }
            return ChatColor.RED + String.format("Warzone %s Team Config", zone.getName());
        } else {
            return ChatColor.RED + "Default Team Config";
        }
    }

    @Override
    public int getSize() {
        if (zone == null && team == null) {
            return 9 * 2;
        }
        return 9 * 3;
    }
}
