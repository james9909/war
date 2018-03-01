package com.tommytony.war.ui;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EditZoneConfigListUI extends ChestUI {

    private Warzone zone;

    EditZoneConfigListUI(Warzone zone) {
        super();
        this.zone = zone;
    }

    @Override
    public void build(Player player, Inventory inv) {
        Runnable editZoneAction = () -> War.war.getUIManager().assignUI(player, new AdminEditZoneConfigUI(zone));
        Runnable editTeamAction = () -> War.war.getUIManager().assignUI(player, new AdminEditTeamConfigUI(zone));

        ItemStack item;
        ItemMeta meta;
        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit Warzone Config");
        item.setItemMeta(meta);
        this.addItem(inv, 0, item, editZoneAction);

        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit Team Config");
        item.setItemMeta(meta);
        this.addItem(inv, 1, item, editTeamAction);
    }

    @Override
    public String getTitle() {
        return String.format("Warzone \"%s\" Config", zone.getName());
    }

    @Override
    public int getSize() {
        return 9;
    }
}
