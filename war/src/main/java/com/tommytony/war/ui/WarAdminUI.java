package com.tommytony.war.ui;

import com.tommytony.war.War;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Connor on 7/27/2017.
 */
public class WarAdminUI extends ChestUI {

    @Override
    public void build(final Player player, Inventory inv) {
        Runnable editWarAction = () -> War.war.getUIManager().assignUI(player, new EditWarConfigUI());
        Runnable editZoneAction = () -> War.war.getUIManager().assignUI(player, new EditZoneConfigUI(null));
        Runnable editTeamAction = () -> War.war.getUIManager().assignUI(player, new EditTeamConfigUI(null, null));
        Runnable editRewardsAction = () -> War.war.getUIManager().assignUI(player, new EditRewardsListUI(null, null));
        Runnable editLoadoutsAction = () -> War.war.getUIManager().assignUI(player, new EditLoadoutsListUI(null, null));

        int i = 0;
        ItemStack item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit War Config", null);
        this.addItem(inv, i++, item, editWarAction);

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Warzone Config", null);
        this.addItem(inv, i++, item, editZoneAction);

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Team Config", null);
        this.addItem(inv, i++, item, editTeamAction);

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Rewards", null);
        this.addItem(inv, i++, item, editRewardsAction);

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Loadouts", null);
        this.addItem(inv, i, item, editLoadoutsAction);
    }

    @Override
    public String getTitle() {
        return ChatColor.DARK_RED + "" + ChatColor.BOLD + "War Admin Panel";
    }

    @Override
    public int getSize() {
        return 9;
    }
}
