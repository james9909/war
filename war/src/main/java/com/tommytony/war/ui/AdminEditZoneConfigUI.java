package com.tommytony.war.ui;

import com.tommytony.war.War;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AdminEditZoneConfigUI extends ChestUI {

    @Override
    public void build(Player player, Inventory inv) {
        UIConfigHelper.addWarzoneConfigOptions(this, player, inv, War.war.getWarzoneDefaultConfig(), null, 0);
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Warzone Default Config";
    }

    @Override
    public int getSize() {
        return 9 * 3;
    }
}
