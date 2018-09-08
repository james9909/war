package com.tommytony.war.listeners;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.WarConfig;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.structure.Bomb;
import com.tommytony.war.structure.Cake;
import com.tommytony.war.structure.Monument;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author tommytony
 */
public class WarBlockListener implements Listener {

    private static final HashMap<Material, String> DROP_NAMES = new HashMap<>();
    static {
        DROP_NAMES.put(Material.TNT, ChatColor.translateAlternateColorCodes('&', "&4Explosives"));
        DROP_NAMES.put(Material.SMOOTH_BRICK, ChatColor.translateAlternateColorCodes('&', "&7Stone Bricks"));
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) {
            return;
        }
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team team = warPlayer.getTeam();

        Warzone zone = Warzone.getZoneByLocation(player);
        // Monument capturing
        if (team != null && zone != null && zone.isMonumentCenterBlock(block) && team.getKind().isTeamBlock(block.getState())) {
            Monument monument = zone.getMonumentFromCenterBlock(block);
            if (monument != null && !monument.hasOwner()) {
                monument.capture(team);
                zone.broadcast("zone.monument.capture", monument.getName(), team.getName());
                event.setCancelled(false);
                return; // important otherwise cancelled down a few line by isImportantblock
            } else {
                War.war.badMsg(player, "zone.monument.badblock");
                cancelAndKeepItem(event);
                return;
            }
        }

        boolean isZoneMaker = War.war.isZoneMaker(player);
        // prevent build in important parts
        if (zone != null && (zone.isImportantBlock(block) || zone.isOpponentSpawnPeripheryBlock(team, block)) && (!isZoneMaker || (isZoneMaker && team != null))) {
            War.war.badMsg(player, "build.denied.location");
            cancelAndKeepItem(event);
            return;
        }

        // Can only build inside a zone when playing
        Warzone blockZone = Warzone.getZoneByLocation(new Location(block.getWorld(), block.getX(), block.getY(), block.getZ()));
        if (zone == null && blockZone != null && War.war.getWarConfig().getBoolean(WarConfig.BUILDINZONESONLY)) {
            if (!War.war.getWarConfig().getBoolean(WarConfig.DISABLEBUILDMESSAGE)) {
                War.war.badMsg(player, "build.denied.location");
            }
            cancelAndKeepItem(event);
            return;
        }

        // Can only build outside a zone when not playing
        if (zone != null && blockZone == null && War.war.getWarConfig().getBoolean(WarConfig.BUILDINZONESONLY)) {
            if (!War.war.getWarConfig().getBoolean(WarConfig.DISABLEBUILDMESSAGE)) {
                War.war.badMsg(player, "build.denied.outside");
            }
            cancelAndKeepItem(event);
            return;
        }

        // can't place a block of your team's color
        if (team != null && block.getType() == team.getKind().getMaterial() && block.getState().getData() == team.getKind().getBlockData()) {
            War.war.badMsg(player, "build.denied.teamblock");
            cancelAndKeepItem(event);
            return;
        }

        // a flag thief can't drop his flag
        if (team != null && zone != null && zone.isFlagThief(warPlayer)) {
            War.war.badMsg(player, "drop.flag.disabled");
            cancelAndKeepItem(event);
            return;
        }

        // a bomb thief can't drop his bomb
        if (team != null && zone != null && zone.isBombThief(warPlayer)) {
            War.war.badMsg(player, "drop.bomb.disabled");
            cancelAndKeepItem(event);
            return;
        }

        // a cake thief can't drop his cake
        if (team != null && zone != null && zone.isCakeThief(warPlayer)) {
            War.war.badMsg(player, "drop.cake.disabled");
            cancelAndKeepItem(event);
            return;
        }

        // unbreakableZoneBlocks
        if (zone != null && (zone.getWarzoneConfig().getBoolean(WarzoneConfig.UNBREAKABLE) || (team != null && !team.getTeamConfig().resolveBoolean(TeamConfig.PLACEBLOCK))) && (!isZoneMaker || (
            isZoneMaker && team != null))) {
            // if the zone is unbreakable, no one but zone makers can break blocks (even then, zone makers in a team can't break blocks)
            War.war.badMsg(player, "build.denied.zone.place");
            cancelAndKeepItem(event);
            return;
        }

        if (team != null && !team.canModify(block.getType())) {
            War.war.badMsg(player, "build.denied.zone.type");
            cancelAndKeepItem(event);
        }
    }

    private void cancelAndKeepItem(BlockPlaceEvent event) {
        event.setCancelled(true);
        ItemStack inHand = event.getItemInHand();
        ItemStack newItemInHand;

        if (inHand.getType() == Material.FIRE) {
            // Weird bukkit/mc behavior where item in hand is reported as fire while using flint & steel.
            // Just give the user his f&s back but almost broken (max durability is 8).
            newItemInHand = new ItemStack(Material.FLINT_AND_STEEL, 1, (short) 1);
        } else {
            newItemInHand = inHand.clone();
        }
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            event.getPlayer().getInventory().setItemInOffHand(newItemInHand);
        } else {
            event.getPlayer().getInventory().setItemInMainHand(newItemInHand);
        }
    }

    @EventHandler
    // Do not allow moving of block into or from important zones
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        Warzone zone = Warzone.getZoneByLocation(event.getBlock().getLocation());
        if (zone != null) {
            for (Block b : event.getBlocks()) {
                if (zone.isImportantBlock(b)) {
                    event.setCancelled(true);
                    return;
                }
            }
            //noinspection deprecation
            if (zone.isImportantBlock(event.getBlock().getRelative(event.getDirection(), event.getLength() + 1))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        Warzone zone = Warzone.getZoneByLocation(event.getBlock().getLocation());
        if (zone != null) {
            Block b = event.getBlock().getRelative(event.getDirection(), 2);
            if (zone.isImportantBlock(b)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player != null && block != null) {
            this.handleBreakOrDamage(player, block, event);
        }
    }

    @EventHandler
    public void onBlockDamage(final BlockDamageEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Warzone playerZone = Warzone.getZoneByLocation(player);
        if (player != null && block != null && playerZone != null && playerZone.getWarzoneConfig().getBoolean(WarzoneConfig.INSTABREAK)) {
            Warzone blockZone = Warzone.getZoneByLocation(new Location(block.getWorld(), block.getX(), block.getY(), block.getZ()));
            if (blockZone != null && blockZone == playerZone && block.getType() != Material.BEDROCK) {
                event.setInstaBreak(true);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(final BlockBurnEvent event) {
        Warzone zone = Warzone.getZoneByLocation(event.getBlock().getLocation());
        if (zone != null && zone.isImportantBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private void handleBreakOrDamage(Player player, Block block, Cancellable event) {
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();
        Team team = warPlayer.getTeam();
        boolean isZoneMaker = War.war.isZoneMaker(player);

        if (zone != null && team == null && !isZoneMaker) {
            // can't actually destroy blocks in a warzone if not part of a team
            War.war.badMsg(player, "build.denied.zone.outside");
            event.setCancelled(true);
            return;
        }
        // monument's center is destroyed
        if (team != null && block != null && zone != null && zone.isMonumentCenterBlock(block)) {
            Monument monument = zone.getMonumentFromCenterBlock(block);
            if (monument.hasOwner()) {
                Team ownerTeam = monument.getOwnerTeam();
                zone.broadcast("zone.monument.lose", ownerTeam.getName(), monument.getName());
                monument.uncapture();
            }
            event.setCancelled(false);
            return;
        }
        // changes in parts of important areas
        if (zone != null && zone.isImportantBlock(block) && (!isZoneMaker || team != null)) {
            // breakage of spawn
            if (team.isSpawnLocation(block.getLocation())) {
                War.war.badMsg(player, "build.denied.zone.spawn", team.getName());
                event.setCancelled(true);
                return;
            }
            // stealing of flag
            if (zone.isEnemyTeamFlagBlock(team, block)) {
                if (zone.isFlagThief(warPlayer)) {
                    // detect audacious thieves
                    War.war.badMsg(player, "zone.stealextra.flag");
                } else if (zone.isBombThief(warPlayer) || zone.isCakeThief(warPlayer)) {
                    War.war.badMsg(player, "zone.stealextra.other");
                } else {
                    Team lostFlagTeam = zone.getTeamForFlagBlock(block);
                    if (lostFlagTeam.getPlayers().size() != 0) {
                        // player just broke the flag block of other team: cancel to avoid drop, give player the block, set block to air
                        ItemStack teamKindBlock = lostFlagTeam.getKind().getBlockHead();
                        player.getInventory().clear();
                        player.getInventory().addItem(teamKindBlock);
                        zone.addFlagThief(lostFlagTeam, warPlayer);
                        block.setType(Material.AIR);
                        for (Team t : zone.getTeams()) {
                            t.teamcast("zone.steal.flag.broadcast", team.getKind().getColor() + player.getName() + ChatColor.WHITE, lostFlagTeam.getName());
                            if (t.getName().equals(lostFlagTeam.getName())) {
                                t.teamcast("zone.steal.flag.prevent", team.getKind().getColor() + player.getName() + ChatColor.WHITE, team.getName());
                            }
                        }
                        War.war.msg(player, "zone.steal.flag.notice", lostFlagTeam.getName());
                    } else {
                        War.war.msg(player, "zone.steal.flag.empty", lostFlagTeam.getName());
                    }
                }
                event.setCancelled(true);
                return;
            } else if (zone.isBombBlock(block)) {
                if (zone.isBombThief(warPlayer)) {
                    // detect audacious thieves
                    War.war.badMsg(player, "zone.stealextra.bomb");
                } else if (zone.isFlagThief(warPlayer) || zone.isCakeThief(warPlayer)) {
                    War.war.badMsg(player, "zone.stealextra.other");
                } else {
                    Bomb bomb = zone.getBombForBlock(block);
                    // player just broke the bomb block: cancel to avoid drop, give player the block, set block to air
                    ItemStack tntBlock = new ItemStack(Material.TNT);
                    tntBlock.setDurability((short) 8);
                    player.getInventory().clear();
                    player.getInventory().addItem(tntBlock);
                    zone.addBombThief(bomb, warPlayer);
                    block.setType(Material.AIR);
                    for (Team t : zone.getTeams()) {
                        t.teamcast("zone.steal.bomb.broadcast", team.getKind().getColor() + player.getName() + ChatColor.WHITE, ChatColor.GREEN + bomb.getName() + ChatColor.WHITE);
                        t.teamcast("zone.steal.bomb.prevent", team.getKind().getColor() + player.getName() + ChatColor.WHITE);
                    }
                    War.war.msg(player, "zone.steal.bomb.notice", bomb.getName());
                }
                event.setCancelled(true);
                return;
            } else if (zone.isCakeBlock(block)) {
                if (zone.isCakeThief(warPlayer)) {
                    // detect audacious thieves
                    War.war.badMsg(player, "zone.stealextra.cake");
                } else if (zone.isFlagThief(warPlayer) || zone.isBombThief(warPlayer)) {
                    War.war.badMsg(player, "zone.stealextra.other");
                } else {
                    Cake cake = zone.getCakeForBlock(block);
                    // player just broke the cake block: cancel to avoid drop, give player the block, set block to air
                    ItemStack cakeBlock = new ItemStack(Material.CAKE);
                    cakeBlock.setDurability((short) 8);
                    player.getInventory().clear();
                    player.getInventory().addItem(cakeBlock);
                    zone.addCakeThief(cake, warPlayer);
                    block.setType(Material.AIR);
                    for (Team t : zone.getTeams()) {
                        t.teamcast("zone.steal.cake.broadcast", team.getKind().getColor() + player.getName() + ChatColor.WHITE, ChatColor.GREEN + cake.getName() + ChatColor.WHITE);
                        t.teamcast("zone.steal.cake.prevent", team.getKind().getColor() + player.getName() + ChatColor.WHITE);
                    }
                    War.war.msg(player, "zone.steal.cake.notice", cake.getName());
                }
                event.setCancelled(true);
                return;
            } else if (!zone.isMonumentCenterBlock(block)) {
                War.war.badMsg(player, "build.denied.location");
                event.setCancelled(true);
                return;
            }
        }

        // protect the hub
        /*
        if (War.war.getWarHub() != null && War.war.getWarHub().getVolume().contains(block)) {
            War.war.badMsg(player, "build.denied.location");
            event.setCancelled(true);
            return;
        }*/

        // buildInZonesOnly
        Warzone blockZone = Warzone.getZoneByLocation(new Location(block.getWorld(), block.getX(), block.getY(), block.getZ()));
        if (blockZone == null && zone != null && War.war.getWarConfig().getBoolean(WarConfig.BUILDINZONESONLY)) {
            if (!War.war.getWarConfig().getBoolean(WarConfig.DISABLEBUILDMESSAGE)) {
                War.war.badMsg(player, "build.denied.outside");
            }
            event.setCancelled(true);
            return;
        }

        // unbreakableZoneBlocks
        if (blockZone != null && blockZone.getWarzoneConfig().getBoolean(WarzoneConfig.UNBREAKABLE) && (!isZoneMaker || (isZoneMaker && team != null))) {
            // if the zone is unbreakable, no one but zone makers can break blocks (even then, zone makers in a team can't break blocks
            War.war.badMsg(player, "build.denied.zone.break");
            event.setCancelled(true);
            return;
        }

        Material blockType = block.getType();
        if (blockZone != null && DROP_NAMES.containsKey(blockType)) {
            if (event instanceof BlockBreakEvent) {
                ItemStack toDrop = new ItemStack(blockType);
                String name = DROP_NAMES.get(blockType);
                ItemMeta meta = toDrop.getItemMeta();
                meta.setDisplayName(name);
                toDrop.setItemMeta(meta);

                block.getWorld().dropItemNaturally(block.getLocation(), toDrop);
                ((BlockBreakEvent) event).setDropItems(false);
                return;
            }
        }

        if (team != null && !team.canModify(block.getType())) {
            War.war.badMsg(player, "build.denied.zone.type");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrowth(final StructureGrowEvent event) {
        Warzone zone = Warzone.getZoneByLocation(event.getLocation());
        if (zone != null) {
            List<BlockState> canceledBlocks = new ArrayList<>();
            for (BlockState state : event.getBlocks()) {
                if (!zone.getVolume().contains(state.getLocation()) || zone.isImportantBlock(state.getBlock())) {
                    canceledBlocks.add(state);
                }
            }
            for (BlockState state : canceledBlocks) {
                event.getBlocks().remove(state);
            }
        }
    }
}

