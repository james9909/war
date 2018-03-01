package com.tommytony.war.ui;

import com.tommytony.war.War;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EditWarConfigUI extends ChestUI {

    @Override
    public void build(Player player, Inventory inv) {
        UIConfigHelper.addWarConfigOptions(this, player, inv, War.war.getWarConfig(), 0);
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "War Default Config";
    }

    @Override
    public int getSize() {
        return 9 * 2;
    }
}
