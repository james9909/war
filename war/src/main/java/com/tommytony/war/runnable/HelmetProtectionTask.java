package com.tommytony.war.runnable;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.WarzoneConfig;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;


/**
 * Sets the helmet again onto the players heads. Also limits the number of blocks being held.
 *
 * @author Tim Düsterhus
 */
public class HelmetProtectionTask implements Runnable {

    public void run() {
        if (!War.war.isLoaded()) {
            return;
        }
        for (Warzone zone : War.war.getWarzones()) {
            for (Team team : zone.getTeams()) {
                for (WarPlayer warPlayer : team.getPlayers()) {
                    Player player = warPlayer.getPlayer();
                    PlayerInventory playerInv = player.getInventory();
                    Material teamBlockMaterial;

                    if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.BLOCKHEADS)) {
                        teamBlockMaterial = team.getKind().getMaterial();
                        // 1) Replace missing block head
                        if (playerInv.getHelmet() == null || playerInv.getHelmet().getType() != Material.WOOL) {
                            ItemStack helmet = team.getKind().getBlockHead();
                            if (!warPlayer.getLoadoutSelection().getSelectedLoadout().equalsIgnoreCase("knight")) {
                                ItemMeta meta = helmet.getItemMeta();
                                meta.addEnchant(Enchantment.PROTECTION_PROJECTILE, 3, true);
                                helmet.setItemMeta(meta);
                            }
                            playerInv.setHelmet(helmet);
                        }

                        // 2) Get rid of extra blocks in inventory: only keep one
                        HashMap<Integer, ? extends ItemStack> blocks = playerInv.all(teamBlockMaterial);
                        if (blocks.size() > 1 || (blocks.size() == 1 && blocks.get(blocks.keySet().iterator().next()).getAmount() > 1)) {
                            int i = 0;
                            int removed = 0;
                            for (ItemStack item : playerInv.getContents()) {
                                // remove only same colored wool
                                if (item != null && item.getType() == teamBlockMaterial && item.getData() == team.getKind().getBlockData()) {
                                    playerInv.clear(i);
                                    removed++;
                                }
                                i++;
                            }

                            int firstEmpty = playerInv.firstEmpty();
                            if (firstEmpty > 0) {
                                playerInv.setItem(firstEmpty, team.getKind().getBlockHead());
                            }

                            if (removed > 1) {
                                War.war.badMsg(player, "All that " + team.getName() + " wool must have been heavy!");
                            }
                        }
                    }

                    // check for thieves without their treasure in their hands
                    if (zone.isFlagThief(warPlayer)) {
                        Team victim = zone.getVictimTeamForFlagThief(warPlayer);
                        player.getInventory().setItemInMainHand(null);
                        player.getInventory().setItemInOffHand(null);
                        player.getInventory().setHeldItemSlot(0);
                        player.getInventory().addItem(victim.getKind().getBlockData().toItemStack(2240));
                    } else if (zone.isBombThief(warPlayer)) {
                        player.getInventory().setItemInMainHand(null);
                        player.getInventory().setItemInOffHand(null);
                        player.getInventory().setHeldItemSlot(0);
                        player.getInventory().addItem(new ItemStack(Material.TNT, 2240));
                    } else if (zone.isCakeThief(warPlayer)) {
                        player.getInventory().setItemInMainHand(null);
                        player.getInventory().setItemInOffHand(null);
                        player.getInventory().setHeldItemSlot(0);
                        player.getInventory().addItem(new ItemStack(Material.CAKE_BLOCK, 2240));
                    }
                }
            }
        }
    }
}
