package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.bags.TeamConfigBag;
import com.tommytony.war.config.bags.WarConfigBag;
import com.tommytony.war.config.bags.WarzoneConfigBag;
import com.tommytony.war.utility.Loadout;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CreateLoadoutUI extends ChestUI {

    private Team team;
    private Warzone zone;

    CreateLoadoutUI(Warzone zone, Team team) {
        this.zone = zone;
        this.team = team;
    }

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

                    if (zone != null) {
                        zone.getDefaultInventories().addLoadout(newLoadout);
                        WarzoneConfigBag.afterUpdate(zone, player, "Loadout saved", false);
                    } else if (team != null) {
                        team.getInventories().addLoadout(newLoadout);
                        TeamConfigBag.afterUpdate(team, player, "Loadout saved", false);
                    } else {
                        War.war.getDefaultInventories().addLoadout(newLoadout);
                        WarConfigBag.afterUpdate(player, "Loadout saved", false);
                    }
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
