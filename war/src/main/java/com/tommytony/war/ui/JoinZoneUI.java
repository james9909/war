package com.tommytony.war.ui;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.WarzoneConfig;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Connor on 7/25/2017.
 */
public class JoinZoneUI extends ChestUI {

    @Override
    public void build(final Player player, Inventory inv) {
        List<String> lore = Arrays.asList(ChatColor.GRAY + "Teleports you to the " + ChatColor.RED + "Warhub" + ChatColor.GRAY + " lobby", ChatColor.DARK_GRAY + "Warzone doors located here");
        String title = ChatColor.RED + "" + ChatColor.BOLD + "Warhub";
        ItemStack item = createItem(Material.TNT, title, lore);

        int i = 0;
        this.addItem(inv, i++, item, () -> player.performCommand("/warhub"));
        for (final Warzone zone : War.war.getEnabledWarzones()) {
            title = ChatColor.YELLOW + "" + ChatColor.BOLD + zone.getName();
            lore = Arrays.asList(
                String.format(ChatColor.GRAY + "%d/%d players", zone.getPlayerCount(), zone.getMaxPlayers()),
                String.format(ChatColor.GRAY + "%d teams", zone.getTeams().size()),
                ChatColor.DARK_GRAY + "Click to join"
            );
            item = createItem(Material.ENDER_PEARL, title, lore);

            this.addItem(inv, i++, item, () -> {
                if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
                    War.war.badMsg(player, "join.disabled");
                } else if (zone.isReinitializing()) {
                    War.war.badMsg(player, "join.disabled");
                } else if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.JOINMIDBATTLE) && zone.isEnoughPlayers()) {
                    War.war.badMsg(player, "join.progress");
                } else if (zone.isFull()) {
                    War.war.badMsg(player, "join.full.all");
                } else {
                    zone.autoAssign(player);
                }
            });
        }
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Pick a Warzone";
    }

    @Override
    public int getSize() {
        int zones = War.war.getEnabledWarzones().size() + 1;
        if (zones % 9 == 0) {
            return zones;
        }
        return (zones / 9 + 1) * 9;
    }
}
