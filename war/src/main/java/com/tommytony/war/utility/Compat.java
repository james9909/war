package com.tommytony.war.utility;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.tommytony.war.War;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Simple fixes to account for removed Bukkit functionality
 */
public class Compat {
    public static class BlockPair {
        final Block block1;
        final Block block2;

        BlockPair(Block block1, Block block2) {
            this.block1 = block1;
            this.block2 = block2;
        }

        public Block getBlock1() {
            return block1;
        }

        public Block getBlock2() {
            return block2;
        }
    }

    public static BlockPair getWorldEditSelection(Player player) {
        if (!War.war.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            return null;
        }

        BukkitPlayer wp = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wp);
        try {
            Region selection = session.getSelection(wp.getWorld());
            if (selection instanceof CuboidRegion) {
                BlockVector3 min = selection.getMinimumPoint();
                BlockVector3 max = selection.getMaximumPoint();
                BlockPair pair = new BlockPair(
                    player.getWorld().getBlockAt(min.getBlockX(), min.getBlockY(), min.getBlockZ()),
                    player.getWorld().getBlockAt(max.getBlockX(), max.getBlockY(), max.getBlockZ())
                );
                return pair;
            }
            return null;
        } catch (IncompleteRegionException e) {
            return null;
        }

    }
}
