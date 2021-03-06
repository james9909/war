package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.bags.InventoryBag;
import com.tommytony.war.config.bags.TeamConfigBag;
import com.tommytony.war.config.bags.WarConfigBag;
import com.tommytony.war.config.bags.WarzoneConfigBag;
import com.tommytony.war.utility.Reward;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditRewardsUI extends ChestUI {

    private Reward reward;
    private Warzone zone;
    private Team team;
    private boolean win;

    public EditRewardsUI(Reward reward, Warzone zone, Team team, boolean win) {
        super();
        this.reward = reward;
        this.zone = zone;
        this.team = team;
        this.win = win;
    }

    @Override
    public void build(Player player, Inventory inv) {
        List<ItemStack> items = new ArrayList<>();
        if (reward != null) {
            items = reward.getRewards();
        }
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) {
                    inv.addItem(item);
                }
            }
        }

        ItemStack item = createSaveItem();
        this.addItem(inv, getSize() - 2, item, () -> {
            List<ItemStack> newItems = new ArrayList<>();
            for (int i = 0; i < getSize()-2; i++) {
                ItemStack slot = inv.getItem(i);
                if (slot != null && slot.getType() != Material.AIR) {
                    newItems.add(slot);
                }
            }

            Reward reward = new Reward(newItems);
            InventoryBag inventoryBag = getInventoryBag();

            if (win) {
                inventoryBag.setWinReward(reward);
            } else {
                inventoryBag.setLossReward(reward);
            }

            updateBag(player);
        });

        item = createDeleteItem();
		this.addItem(inv, getSize() - 1, item, () -> {
		    InventoryBag inventoryBag = getInventoryBag();

            if (win) {
                inventoryBag.setWinReward(null);
            } else {
                inventoryBag.setLossReward(null);
            }

            updateBag(player);
        });
    }

    public void updateBag(Player player) {
        if (zone != null) {
            WarzoneConfigBag.afterUpdate(zone, player, "Rewards updated", false);
        } else if (team != null) {
            TeamConfigBag.afterUpdate(team, player, "Rewards updated", false);
        } else {
            WarConfigBag.afterUpdate(player, "Rewards updated", false);
        }
    }

    public InventoryBag getInventoryBag() {
        if (zone != null) {
            return zone.getDefaultInventories();
        } else if (team != null) {
            return team.getInventories();
        } else {
            return War.war.getDefaultInventories();
        }
    }

    @Override
    public String getTitle() {
        if (win) {
            return ChatColor.RED + "Updating win reward";
        } else {
            return ChatColor.RED + "Updating loss reward";
        }
    }

    @Override
    public int getSize() {
        return 9;
    }
}
