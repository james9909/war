package com.tommytony.war.ui;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.bags.WarzoneConfigBag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditZoneConfigUI extends ChestUI {

    private Warzone zone;

    EditZoneConfigUI(Warzone zone) {
        super();
        this.zone = zone;
    }

    @Override
    public void build(Player player, Inventory inv) {
        WarzoneConfigBag config;
        if (zone == null) {
            config = War.war.getWarzoneDefaultConfig();
        } else {
            config = zone.getWarzoneConfig();
        }
        UIConfigHelper.addWarzoneConfigOptions(this, player, inv, config, zone, 0);

        if (zone != null) {
            String title = ChatColor.GRAY + "" + ChatColor.BOLD + "Restore Defaults";
            ItemStack item = createItem(Material.SNOW_BALL, title, null);
            this.addItem(inv, getSize() - 1, item, () -> {
                zone.getWarzoneConfig().reset();
                WarzoneConfigBag.afterUpdate(zone, player, "All warzone options set to defaults in warzone " + zone.getName() + " by " + player.getName(), false);
                War.war.getUIManager().assignUI(player, new EditZoneConfigUI(zone));
            });
        }
    }

    @Override
    public String getTitle() {
        if (zone == null) {
            return ChatColor.RED + "Warzone Default Config";
        }
        return ChatColor.RED + String.format("Warzone \"%s\" Config", zone.getName());
    }

    @Override
    public int getSize() {
        if (zone == null) {
            return 9 * 3;
        }
        return 9 * 4;
    }
}
