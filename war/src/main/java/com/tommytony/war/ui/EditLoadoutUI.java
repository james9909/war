package com.tommytony.war.ui;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.War;
import com.tommytony.war.config.bags.WarConfigBag;
import com.tommytony.war.utility.Loadout;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import java.util.Arrays;

public class EditLoadoutUI extends ChestUI {

    private Loadout loadout;

    EditLoadoutUI(Loadout loadout) {
        this.loadout = loadout;
    }

    @Override
    public void build(Player player, Inventory inv) {
        for (ItemStack loadoutItem : loadout.getItems()) {
            if (loadoutItem != null) {
                inv.addItem(loadoutItem);
            }
        }

        this.addItem(inv, 9*4, loadout.getOffhand(), null);
        this.addItem(inv, 9*4+1, loadout.getHelmet(), null);
        this.addItem(inv, 9*4+2, loadout.getChestplate(), null);
        this.addItem(inv, 9*4+3, loadout.getLeggings(), null);
        this.addItem(inv, 9*4+4, loadout.getBoots(), null);

        ItemStack item = new Dye(loadout.getDefault() ? DyeColor.LIME : DyeColor.GRAY).toItemStack(1);
        ItemMeta meta = item.getItemMeta();
        String name = "Value: " + (loadout.getDefault() ? ChatColor.GREEN + "true" : ChatColor.DARK_GRAY + "false");
        meta.setDisplayName("Default");
        meta.setLore(new ImmutableList.Builder<String>().add(name).build());
        item.setItemMeta(meta);
        this.addItem(inv, getSize() - 3, item, () -> {
            loadout.setDefault(!loadout.getDefault());
            if (loadout.getDefault()) {
                War.war.getDefaultInventories().addLoadout(loadout.getName());
            } else {
                War.war.getDefaultInventories().removeLoadout(loadout.getName());
            }
            WarConfigBag.afterUpdate(player, "Loadout updated", false);
            War.war.getUIManager().assignUI(player, new EditLoadoutUI(loadout));
        });

        item = createSaveItem();
        this.addItem(inv, getSize() - 2, item, () -> {
            ItemStack[] contents = inv.getContents();
            contents = Arrays.copyOfRange(contents, 0, 9*4+5);

            loadout.setItemsFromItemList(contents);

            WarConfigBag.afterUpdate(player, "Loadout updated", false);
        });

        item = createDeleteItem();
        this.addItem(inv, getSize() - 1, item, () -> {
            War.war.getDefaultInventories().removeLoadout(loadout.getName());
            WarConfigBag.afterUpdate(player, "Loadout deleted", false);
        });
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + String.format("Edit loadout %s", loadout.getName());
    }

    @Override
    public int getSize() {
        return 9*5;
    }
}
