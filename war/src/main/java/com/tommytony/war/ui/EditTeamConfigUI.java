package com.tommytony.war.ui;

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

    EditTeamConfigUI(Warzone zone) {
        super();
        this.zone = zone;
    }

    @Override
    public void build(Player player, Inventory inv) {
        TeamConfigBag config;
        if (zone == null) {
            config = War.war.getTeamDefaultConfig();
        } else {
            config = zone.getTeamDefaultConfig();
        }
        UIConfigHelper.addTeamConfigOptions(this, player, inv, config, null, zone, 0);

        if (zone != null) {
            ItemStack item;
            ItemMeta meta;
            item = new ItemStack(Material.SNOW_BALL);
            meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Restore Defaults");
            item.setItemMeta(meta);
            this.addItem(inv, getSize() - 1, item, () -> {
                zone.getTeamDefaultConfig().reset();
                WarzoneConfigBag.afterUpdate(zone, player, "All team options set to defaults in warzone " + zone.getName() + " by " + player.getName(), false);
                War.war.getUIManager().assignUI(player, new EditZoneConfigListUI(zone));
            });
        }
    }

    @Override
    public String getTitle() {
        if (zone == null) {
            return ChatColor.RED + "Default Team Config";
        }
        return ChatColor.RED + String.format("Warzone \"%s\" Team Config", zone.getName());
    }

    @Override
    public int getSize() {
        if (zone == null) {
            return 9 * 2;
        }
        return 9 * 3;
    }
}
