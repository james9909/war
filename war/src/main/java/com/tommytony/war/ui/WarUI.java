package com.tommytony.war.ui;

import com.tommytony.war.War;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Connor on 7/26/2017.
 */
public class WarUI extends ChestUI {

    @Override
    public void build(final Player player, Inventory inv) {
        Runnable joinZoneAction = () -> War.war.getUIManager().assignUI(player, new JoinZoneUI());
        Runnable createZoneAction = () -> War.war.getUIManager().assignUI(player, new EditOrCreateZoneUI());
        Runnable warAdminAction = () -> War.war.getUIManager().assignUI(player, new WarAdminUI());

        if (War.war.isWarAdmin(player)) {
            this.addItem(inv, 2, getWarAdminItem(), warAdminAction);
            this.addItem(inv, 4, getCreateWarzoneItem(), createZoneAction);
            this.addItem(inv, 6, getJoinWarzoneItem(), joinZoneAction);
        } else if (War.war.isZoneMaker(player)) {
            this.addItem(inv, 2, getCreateWarzoneItem(), createZoneAction);
            this.addItem(inv, 6, getJoinWarzoneItem(), joinZoneAction);
        } else {
            this.addItem(inv, 4, getJoinWarzoneItem(), joinZoneAction);
        }
    }

    private ItemStack getCreateWarzoneItem() {
        String title = ChatColor.YELLOW + "" + ChatColor.BOLD + "Create Warzone";
        List<String> lore = Collections.singletonList(ChatColor.GRAY + "Click to create, or edit a " + ChatColor.AQUA + "Warzone" + ChatColor.GRAY + ".");
        return createItem(Material.WOODEN_AXE, title, lore);
    }

    private ItemStack getJoinWarzoneItem() {
        String title = ChatColor.RED + "" + ChatColor.BOLD + "Join Warzone";
        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Click to access " + ChatColor.AQUA + "Warzones" + ChatColor.GRAY + ".",
            ChatColor.DARK_GRAY + "Play in PVP areas, with multiple gamemodes here."
        );
        return createItem(Material.IRON_SWORD, title, lore);
    }

    private ItemStack getWarAdminItem() {
        String title = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Manage War";
        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Click to display " + ChatColor.DARK_RED + "Admin" + ChatColor.GRAY + " access panel",
            ChatColor.GRAY + "Includes: " + ChatColor.DARK_GRAY + "Permissions, managing warzones, configs, etc."
        );
        return createItem(Material.ENDER_EYE, title, lore);
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "War";
    }

    @Override
    public int getSize() {
        return 9;
    }
}
