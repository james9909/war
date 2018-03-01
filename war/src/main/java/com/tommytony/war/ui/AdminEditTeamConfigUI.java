package com.tommytony.war.ui;

import com.tommytony.war.War;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AdminEditTeamConfigUI extends ChestUI {

    @Override
    public void build(Player player, Inventory inv) {
        UIConfigHelper.addTeamConfigOptions(this, player, inv, War.war.getTeamDefaultConfig(), null, null, 0);
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Default Team Config";
    }

    @Override
    public int getSize() {
        return 9 * 2;
    }
}
