package com.tommytony.war.ui;

import com.tommytony.war.War;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Connor on 7/27/2017.
 */
public class WarAdminUI extends ChestUI {

    @Override
    public void build(final Player player, Inventory inv) {
        Runnable editWarAction = () -> War.war.getUIManager().assignUI(player, new EditWarConfigUI());
        Runnable editZoneAction = () -> War.war.getUIManager().assignUI(player, new EditZoneConfigUI(null));
        Runnable editTeamAction = () -> War.war.getUIManager().assignUI(player, new EditTeamConfigUI(null));
        Runnable editRewardsAction = () -> War.war.getUIManager().assignUI(player, new EditRewardsListUI(null, null));

        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit War Config");
        item.setItemMeta(meta);
        this.addItem(inv, 0, item, editWarAction);

        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit Warzone Config");
        item.setItemMeta(meta);
        this.addItem(inv, 1, item, editZoneAction);

        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit Team Config");
        item.setItemMeta(meta);
        this.addItem(inv, 2, item, editTeamAction);

        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit Rewards");
        item.setItemMeta(meta);
        this.addItem(inv, 3, item, editRewardsAction);
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
