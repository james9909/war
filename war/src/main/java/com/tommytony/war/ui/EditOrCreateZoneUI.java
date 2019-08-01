package com.tommytony.war.ui;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.ZoneSetter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Created by Connor on 7/27/2017.
 */
public class EditOrCreateZoneUI extends ChestUI {

    @Override
    public void build(final Player player, Inventory inv) {
        int i = 0;
        List<String> lore = Collections.singletonList(ChatColor.GRAY + "Click to create a " + ChatColor.AQUA + "Warzone");
        String title = ChatColor.BOLD + "" + ChatColor.YELLOW + "Create Warzone";
        ItemStack item = createItem(Material.WOODEN_AXE, title, lore);
        this.addItem(inv, i++, item, new Runnable() {
            @Override
            public void run() {
                if (!War.war.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                    player.sendTitle("", ChatColor.RED + "This feature requires WorldEdit.", 10, 20, 10);
                    return;
                }
                player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE, 1));
                War.war.getUIManager().getPlayerMessage(player, "Select region for zone using WorldEdit and then type a name:", new StringRunnable() {
                    @Override
                    public void run() {
                        WorldEditPlugin worldEdit = (WorldEditPlugin) War.war.getServer().getPluginManager().getPlugin("WorldEdit");
                        if (worldEdit == null) {
                            return;
                        }
                        try {
                            Region selection = worldEdit.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
                            if (selection instanceof CuboidRegion) {
                                BlockVector3 minBlockVector = selection.getMinimumPoint();
                                BlockVector3 maxBlockVector = selection.getMaximumPoint();
                                Location min = new Location(player.getWorld(), minBlockVector.getBlockX(), minBlockVector.getBlockY(), minBlockVector.getBlockZ());
                                Location max = new Location(player.getWorld(), maxBlockVector.getBlockX(), maxBlockVector.getBlockY(), maxBlockVector.getBlockZ());
                                ZoneSetter setter = new ZoneSetter(player, this.getValue());
                                setter.placeCorner1(min.getBlock());
                                setter.placeCorner2(max.getBlock());
                            }
                        } catch (IncompleteRegionException ignored) { }
                    }
                });
            }
        });

        for (final Warzone zone : War.war.getEnabledWarzones()) {
            if (!War.war.isWarAdmin(player) && !zone.isAuthor(player)) {
                continue;
            }
            title = ChatColor.YELLOW + "" + ChatColor.BOLD + zone.getName();
            lore = Collections.singletonList(ChatColor.GRAY + "Click to edit");
            item = createItem(Material.WRITABLE_BOOK, title, lore);
            this.addItem(inv, i++, item, () -> War.war.getUIManager().assignUI(player, new EditZoneUI(zone)));
        }
    }

    @Override
    public String getTitle() {
        return ChatColor.RED + "Edit or Create Zones";
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
