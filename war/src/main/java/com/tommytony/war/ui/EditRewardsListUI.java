package com.tommytony.war.ui;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.utility.Reward;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EditRewardsListUI extends ChestUI {

    private Warzone zone;
    private Team team;

    public EditRewardsListUI(Warzone zone, Team team) {
        this.zone = zone;
        this.team = team;
    }

    @Override
    public void build(Player player, Inventory inv) {
        Reward winReward;
        Reward lossReward;
        if (zone != null) {
            winReward = zone.getDefaultInventories().getWinReward();
            lossReward = zone.getDefaultInventories().getLossReward();
        } else if (team != null) {
            winReward = team.getInventories().getWinReward();
            lossReward = team.getInventories().getLossReward();
        } else {
            winReward = War.war.getDefaultInventories().getWinReward();
            lossReward = War.war.getDefaultInventories().getLossReward();
        }

        Reward finalWinReward = winReward;
        Runnable editWinRewardAction = () -> War.war.getUIManager().assignUI(player, new EditRewardsUI(finalWinReward, zone, team, true));
        Reward finalLossReward = lossReward;
        Runnable editLossRewardAction = () -> War.war.getUIManager().assignUI(player, new EditRewardsUI(finalLossReward, zone, team, false));

        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit Win Reward");
        item.setItemMeta(meta);
        this.addItem(inv, 0, item, editWinRewardAction);

        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Edit Loss Reward");
        item.setItemMeta(meta);
        this.addItem(inv, 1, item, editLossRewardAction);
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Edit Rewards";
    }

    @Override
    public int getSize() {
        return 9;
    }
}
