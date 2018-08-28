package com.tommytony.war.mapper;

import com.tommytony.war.utility.Reward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RewardYmlMapper {

    public static Reward fromConfigToReward(ConfigurationSection config, String name) {
        if (config == null) {
            return new Reward();
        }
        List<?> itemsList = config.getList(name + ".items");
        List<ItemStack> items = (ArrayList<ItemStack>) itemsList;
        return new Reward(items);
    }

    public static void fromRewardToConfig(ConfigurationSection config, String name, Reward reward) {
        ConfigurationSection section = config.createSection(name);
        section.set("items", reward.getRewards());
    }
}
