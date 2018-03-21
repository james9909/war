package com.tommytony.war;

import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.stats.StatTracker;
import com.tommytony.war.utility.Loadout;
import com.tommytony.war.utility.LoadoutSelection;
import com.tommytony.war.utility.PlayerState;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;

public class WarPlayer {
    private static final Map<UUID, WarPlayer> totalPlayers = new ConcurrentHashMap<>();

    private UUID uuid;
    private Team team;
    private Warzone zone;
    private PlayerState playerState;
    private LoadoutSelection loadoutSelection;
    private int killCount;
    private int deathCount;
    private StatTracker statTracker;

    public WarPlayer(UUID uuid) {
        this.uuid = uuid;
        killCount = 0;
        deathCount = 0;
        statTracker = new StatTracker(getPlayer().getName());
        totalPlayers.put(uuid, this);
    }

    public static WarPlayer getPlayer(UUID uuid) {
        if (!totalPlayers.containsKey(uuid)) {
            totalPlayers.put(uuid, new WarPlayer(uuid));
        }
        return totalPlayers.get(uuid);
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
            playerInv.setHelmet(team.getKind().getHat());
        }
    }

    public void reset() {
        this.team = null;
        this.zone = null;
        this.statTracker.reset();
        resetKillCount();
        resetDeaths();
    }

    public static void removePlayer(Player player) {
        totalPlayers.remove(player.getUniqueId());
    }

    public static Set<WarPlayer> getTotalPlayers() {
        return new HashSet<>(totalPlayers.values());
    }

    public void addKill(Player defender) {
        this.killCount++;
        this.statTracker.addKill(defender);
    }

    public void resetKillCount() {
        this.killCount = 0;
    }

    public int getKills() {
        return killCount;
    }

    public void addDeath() {
        this.deathCount++;
        this.statTracker.addDeath();
    }

    public void resetDeaths() {
        this.deathCount = 0;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void addHeal(Player target, double amount) {
        this.statTracker.addHeal(target, amount);
    }

    public StatTracker getStatTracker() {
        return statTracker;
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
}
