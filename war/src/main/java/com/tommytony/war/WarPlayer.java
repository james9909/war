package com.tommytony.war;

import com.nisovin.magicspells.util.Util;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.stats.StatManager;
import com.tommytony.war.utility.LastDamager;
import com.tommytony.war.utility.Loadout;
import com.tommytony.war.utility.LoadoutSelection;
import com.tommytony.war.utility.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarPlayer {
    private static final Map<UUID, WarPlayer> totalPlayers = new ConcurrentHashMap<>();

    private PermissionAttachment permissions;
    private UUID uuid;
    private Team team;
    private Warzone zone;
    private PlayerState playerState;
    private LoadoutSelection loadoutSelection;
    private int killCount;
    private int deathCount;
    private int healCount;

    private boolean spectating;

    private LastDamager lastDamager;

    public WarPlayer(UUID uuid) {
        this.uuid = uuid;
        this.killCount = 0;
        this.deathCount = 0;
        this.healCount = 0;
        totalPlayers.put(uuid, this);
        this.lastDamager = new LastDamager();
        this.spectating = false;
        this.permissions = getPlayer().addAttachment(War.war);
    }

    public static WarPlayer getPlayer(UUID uuid) {
        if (!totalPlayers.containsKey(uuid)) {
            totalPlayers.put(uuid, new WarPlayer(uuid));
        }
        return totalPlayers.get(uuid);
    }

    public static void removePlayer(Player player) {
        totalPlayers.remove(player.getUniqueId());
    }

    public static Set<WarPlayer> getTotalPlayers() {
        return new HashSet<>(totalPlayers.values());
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Warzone getZone() {
        return zone;
    }

    public void setZone(Warzone zone) {
        this.zone = zone;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public LoadoutSelection getLoadoutSelection() {
        if (loadoutSelection == null) {
            loadoutSelection = new LoadoutSelection(true, "Sorcerer");
        }
        return loadoutSelection;
    }

    public void setLoadoutSelection(LoadoutSelection loadoutSelection) {
        this.loadoutSelection = loadoutSelection;
    }

    public void resetInventory(Loadout loadout) {
        Player player = this.getPlayer();
        // Reset inventory to loadout
        PlayerInventory playerInv = player.getInventory();
        playerInv.clear();
        playerInv.clear(playerInv.getSize());
        playerInv.clear(playerInv.getSize() + 1);
        playerInv.clear(playerInv.getSize() + 2);
        playerInv.clear(playerInv.getSize() + 3); // helmet/blockHead

        if (loadout != null) {
            loadout.giveItems(player);
        }
        if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.BLOCKHEADS)) {
            ItemStack helmet = Util.getItemStackFromString(String.format("War_%s%s", team.getKind().getCapsName(), loadoutSelection.getSelectedLoadout()));
            if (helmet == null) {
                helmet = team.getKind().getBlockHead();
            }
            if (!getLoadoutSelection().getSelectedLoadout().equalsIgnoreCase("knight")) {
                ItemMeta meta = helmet.getItemMeta();
                meta.addEnchant(Enchantment.PROTECTION_PROJECTILE, 3, true);
                helmet.setItemMeta(meta);
            }
            playerInv.setHelmet(helmet);
        }
    }

    public void reset() {
        this.team = null;
        this.zone = null;
        resetStats();
    }

    public void resetStats() {
        this.killCount = 0;
        this.deathCount = 0;
        this.healCount = 0;
    }

    public void addKill(Player defender) {
        this.killCount++;
        StatManager.addKill(getPlayer(), defender);
    }

    public int getKills() {
        return killCount;
    }

    public void addDeath() {
        this.deathCount++;
        StatManager.addDeath(getPlayer());
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void addHeal(Player target, double amount) {
        this.healCount++;
        StatManager.addHeal(getPlayer(), target, amount);
    }

    public int getHeals() {
        return healCount;
    }

    public void savePlayerState() {
        Player player = getPlayer();
        playerState = new PlayerState(player);

        // Store inventory
        try {
            War.war.getInventoryManager().saveInventory(player);
        } catch (Exception e) {
            e.printStackTrace();
            War.war.getLogger().severe("Failed to store inventory for " + player.getName());
        }
    }

    public void restorePlayerState() {
        Player player = getPlayer();
        try {
            War.war.getInventoryManager().restoreInventory(player);
        } catch (Exception e) {
            e.printStackTrace();
            War.war.getLogger().severe("Failed to restore inventory for " + player.getName());
        }

        Location spawn = player.getWorld().getSpawnLocation();
        if (playerState != null) {
            // prevent item hacking through crafting personal inventory slots
            this.preventItemHacking();

            playerState.resetPlayer(player);
            loadoutSelection = null;
        }
        player.teleport(spawn);
    }

    public void preventItemHacking() {
        Player player = getPlayer();
        InventoryView openedInv = player.getOpenInventory();
        if (openedInv.getType() == InventoryType.CRAFTING) {
            // prevent abuse of personal crafting slots (this behavior doesn't seem to happen
            // for containers like workbench and furnace - those get closed properly)
            openedInv.getTopInventory().clear();
        }

        // Prevent player from keeping items he was transferring in his inventory
        openedInv.setCursor(null);
    }

    public LastDamager getLastDamager() {
        return this.lastDamager;
    }

    public void setLastDamager(Player attacker, Entity damager) {
        this.lastDamager.setAttacker(attacker, damager);
    }

    public boolean isSpectating() {
        return spectating;
    }

    public void setSpectating(boolean spectating) {
        this.spectating = spectating;
    }

    public PermissionAttachment getPermissions() {
        if (permissions == null) {
            permissions = getPlayer().addAttachment(War.war);
        }
        return permissions;
    }
}
