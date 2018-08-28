package com.tommytony.war.ui;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditZoneConfigListUI extends ChestUI {

    private Warzone zone;

    EditZoneConfigListUI(Warzone zone) {
        super();
        this.zone = zone;
    }

    @Override
    public void build(Player player, Inventory inv) {
        Runnable editZoneAction = () -> War.war.getUIManager().assignUI(player, new EditZoneConfigUI(zone));
        Runnable editTeamAction = () -> War.war.getUIManager().assignUI(player, new EditTeamConfigUI(zone, null));

        ItemStack item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Warzone Config", null);
        this.addItem(inv, 0, item, editZoneAction);

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Edit Team Config", null);
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
