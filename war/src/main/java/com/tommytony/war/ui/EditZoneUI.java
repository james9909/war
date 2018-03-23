package com.tommytony.war.ui;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.zonemaker.DeleteZoneCommand;
import com.tommytony.war.command.zonemaker.ResetZoneCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Connor on 7/27/2017.
 */
class EditZoneUI extends ChestUI {

    private final Warzone zone;

    EditZoneUI(Warzone zone) {
        super();
        this.zone = zone;
    }

    @Override
    public void build(final Player player, Inventory inv) {
        ItemStack item = createItem(Material.CHEST, ChatColor.YELLOW + "Options", null);
        this.addItem(inv, 0, item, () -> War.war.getUIManager().assignUI(player, new EditZoneConfigListUI(zone)));

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Rewards", null);
        this.addItem(inv, 1, item, () -> War.war.getUIManager().assignUI(player, new EditRewardsListUI(zone, null)));

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Teams", null);
        this.addItem(inv, 2, item, () -> War.war.getUIManager().assignUI(player, new EditTeamsListUI(zone)));

        item = createItem(Material.CHEST, ChatColor.YELLOW + "Loadouts", null);
        this.addItem(inv, 3, item, () -> War.war.getUIManager().assignUI(player, new EditLoadoutsListUI(zone, null)));

        // item = new ItemStack(Material.CHEST);
        // meta = item.getItemMeta();
        // meta.setDisplayName(ChatColor.YELLOW + "Structures");
        // item.setItemMeta(meta);

        item = createItem(Material.NETHER_STAR, ChatColor.GRAY + "Reset Blocks", null);
        this.addItem(inv, 7, item, () -> ResetZoneCommand.forceResetZone(zone, player));

        item = createDeleteItem();
        this.addItem(inv, 8, item, new Runnable() {
            @Override
            public void run() {
                War.war.getUIManager().getPlayerMessage(player, "Delete zone: are you sure? Type \"" + zone.getName() + "\" to confirm:", new StringRunnable() {
                    @Override
                    public void run() {
                        if (this.getValue().equalsIgnoreCase(zone.getName())) {
                            DeleteZoneCommand.forceDeleteZone(zone, player);
                        } else {
                            War.war.badMsg(player, "Delete aborted.");
                        }
                    }
                });
            }
        });
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Editing Warzone \"" + zone.getName() + "\"";
    }

    @Override
    public int getSize() {
        return 9;
    }
}
