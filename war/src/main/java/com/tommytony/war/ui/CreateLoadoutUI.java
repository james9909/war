package com.tommytony.war.ui;

import com.tommytony.war.War;
import com.tommytony.war.config.bags.WarConfigBag;
import com.tommytony.war.utility.Loadout;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class CreateLoadoutUI extends ChestUI {
    @Override
    public void build(Player player, Inventory inv) {
        ItemStack item = createSaveItem();
        this.addItem(inv, getSize() - 1, item, () -> {
            ItemStack[] contents = inv.getContents();
            contents = Arrays.copyOfRange(contents, 0, 9*4+5);
            ItemStack[] items = Arrays.copyOfRange(contents, 0, contents.length-5);
            ItemStack offhand = contents[contents.length-5];
            ItemStack[] armor = Arrays.copyOfRange(contents, contents.length-4, contents.length);

            War.war.getUIManager().getPlayerMessage(player, "Type in the loadout name:", new StringRunnable() {
                @Override
                public void run() {
                    Loadout newLoadout = new Loadout(this.getValue(), items);
                    newLoadout.setArmor(armor);
                    newLoadout.setOffhand(offhand);

                    War.war.addLoadout(newLoadout);
                    WarConfigBag.afterUpdate(player, "Loadout saved", false);
                }
            });
        });
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Create loadout";
    }

    @Override
    public int getSize() {
        return 9*5;
    }
}
