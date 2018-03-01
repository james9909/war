package com.tommytony.war.utility;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Reward {

    private List<ItemStack> rewards;

    public Reward() {
        this.rewards = new ArrayList<>();
    }

    public Reward(List<ItemStack> rewards) {
        this.rewards = rewards;
    }

    public void rewardPlayer(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : rewards) {
            if (item != null) {
                inventory.addItem(item);
            }
        }
    }

    public boolean hasRewards() {
        return rewards != null && !rewards.isEmpty();
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }
}
