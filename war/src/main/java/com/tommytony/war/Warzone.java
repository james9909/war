package com.tommytony.war;

import com.google.common.collect.ImmutableList;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaChangeReason;
import com.tommytony.war.config.InventoryBag;
import com.tommytony.war.config.ScoreboardType;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.TeamConfigBag;
import com.tommytony.war.config.TeamKind;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.config.WarzoneConfigBag;
import com.tommytony.war.event.WarBattleWinEvent;
import com.tommytony.war.event.WarPlayerLeaveEvent;
import com.tommytony.war.event.WarPlayerThiefEvent;
import com.tommytony.war.event.WarScoreCapEvent;
import com.tommytony.war.job.InitZoneJob;
import com.tommytony.war.job.LoadoutResetJob;
import com.tommytony.war.job.LogKillsDeathsJob;
import com.tommytony.war.job.LogKillsDeathsJob.KillsDeathsRecord;
import com.tommytony.war.job.TeleportToSpawnTimer;
import com.tommytony.war.job.ZoneTimeJob;
import com.tommytony.war.mapper.VolumeMapper;
import com.tommytony.war.mapper.ZoneVolumeMapper;
import com.tommytony.war.structure.Bomb;
import com.tommytony.war.structure.Cake;
import com.tommytony.war.structure.CapturePoint;
import com.tommytony.war.structure.Monument;
import com.tommytony.war.structure.WarzoneMaterials;
import com.tommytony.war.structure.ZonePortal;
import com.tommytony.war.structure.ZoneWallGuard;
import com.tommytony.war.utility.Direction;
import com.tommytony.war.utility.Loadout;
import com.tommytony.war.utility.LoadoutSelection;
import com.tommytony.war.utility.PotionEffectHelper;
import com.tommytony.war.utility.Reward;
import com.tommytony.war.volume.Volume;
import com.tommytony.war.volume.ZoneVolume;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * @author tommytony
 * @package com.tommytony.war
 */
public class Warzone {

    private final Set<Team> teams = new HashSet<>();
    private final Set<Monument> monuments = new HashSet<>();
    private final Set<CapturePoint> capturePoints = new HashSet<>();
    private final Set<Bomb> bombs = new HashSet<>();
    private final Set<Cake> cakes = new HashSet<>();
    private final Set<String> authors = new HashSet<>();

    private final int minSafeDistanceFromWall = 6;
    private final Set<WarPlayer> respawn = new HashSet<>();
    private final Set<UUID> reallyDeadFighters = new HashSet<>();
    private final WarzoneConfigBag warzoneConfig;
    private final TeamConfigBag teamDefaultConfig;
    private String name;
    private ZoneVolume volume;
    private World world;
    private Location teleport;
    private Location rallyPoint;
    private List<ZoneWallGuard> zoneWallGuards = new ArrayList<>();
    private Map<UUID, Team> flagThieves = new HashMap<>();
    private Map<UUID, Bomb> bombThieves = new HashMap<>();
    private Map<UUID, Cake> cakeThieves = new HashMap<>();
    private Map<UUID, PermissionAttachment> attachments = new HashMap<>();
    private List<LogKillsDeathsJob.KillsDeathsRecord> killsDeathsTracker = new ArrayList<>();
    private InventoryBag defaultInventories = new InventoryBag();
    private List<ZonePortal> portals = new ArrayList<>();

    private WarzoneMaterials warzoneMaterials = new WarzoneMaterials(new ItemStack(Material.OBSIDIAN), new ItemStack(Material.FENCE), new ItemStack(Material.GLOWSTONE));

    private boolean isEndOfGame = false;
    private boolean isReinitializing = false;
    //private final Object gameEndLock = new Object();

    private boolean pvpReady = true;
    private Random killSeed = new Random();
    private ScoreboardType scoreboardType;

    public Warzone(World world, String name) {
        this.world = world;
        this.name = name;
        this.warzoneConfig = new WarzoneConfigBag(this);
        this.teamDefaultConfig = new TeamConfigBag();    // don't use ctor with Warzone, as this changes config resolution
        this.volume = new ZoneVolume(name, this.getWorld(), this);
        this.pvpReady = true;
        this.scoreboardType = this.getWarzoneConfig().getScoreboardType(WarzoneConfig.SCOREBOARD);
    }

    public static Warzone getZoneByName(String name) {
        Warzone bestGuess = null;
        for (Warzone warzone : War.war.getWarzones()) {
            if (warzone.getName().toLowerCase().equals(name.toLowerCase())) {
                // perfect match, return right away
                return warzone;
            } else if (warzone.getName().toLowerCase().startsWith(name.toLowerCase())) {
                // perhaps there's a perfect match in the remaining zones, let's take this one aside
                bestGuess = warzone;
            }
        }
        return bestGuess;
    }

    public static Warzone getZoneByNameExact(String name) {
        for (Warzone zone : War.war.getWarzones()) {
            if (zone.getName().equalsIgnoreCase(name)) {
                return zone;
            }
        }
        return null;
    }

    public static Warzone getZoneByLocation(Location location) {
        for (Warzone warzone : War.war.getWarzones()) {
            if (location.getWorld().getName().equals(warzone.getWorld().getName()) && warzone.getVolume() != null && warzone.getVolume().contains(location)) {
                return warzone;
            }
        }
        return null;
    }

    public static Warzone getZoneByLocation(Player player) {
        return Warzone.getZoneByLocation(player.getLocation());
    }

    public static Warzone getZoneByPlayerUUID(UUID uuid) {
        return WarPlayer.getPlayer(uuid).getZone();
    }

    public static Warzone getZoneForDeadPlayer(Player player) {
        for (Warzone warzone : War.war.getWarzones()) {
            if (warzone.getReallyDeadFighters().contains(player.getUniqueId())) {
                return warzone;
            }
        }
        return null;
    }

    public boolean ready() {
        return this.volume.hasTwoCorners() && !this.volume.tooSmall() && !this.volume.tooBig();
    }

    public Set<Team> getTeams() {
        return this.teams;
    }

    public Team getPlayerTeam(UUID uuid) {
        // for (Team team : this.teams) {
        //     for (Player player : team.getPlayers()) {
        //         if (player.getName().equals(playerName)) {
        //             return team;
        //         }
        //     }
        // }
        return null;
    }

    public String getTeamInformation() {
        StringBuilder teamsMessage = new StringBuilder(War.war.getString("zone.teaminfo.prefix"));
        if (this.getTeams().isEmpty()) {
            teamsMessage.append(War.war.getString("zone.teaminfo.none"));
        } else {
            for (Team team : this.getTeams()) {
                teamsMessage.append('\n');
                teamsMessage.append(MessageFormat.format(War.war.getString("zone.teaminfo.format"), team.getName(), team.getPoints(), team.getRemainingLives(), team.getTeamConfig().resolveInt(TeamConfig.LIFEPOOL), StringUtils.join(team.getPlayerNames().iterator(), ", ")));
            }
        }
        return teamsMessage.toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
        this.volume.setName(newName);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public Location getTeleport() {
        return this.teleport;
    }

    public void setTeleport(Location location) {
        this.teleport = location;
    }

    public int saveState(boolean clearArtifacts) {
        if (this.ready()) {
            if (clearArtifacts) {
                // removed everything to keep save clean
                for (ZoneWallGuard guard : this.zoneWallGuards) {
                    guard.deactivate();
                }
                this.zoneWallGuards.clear();

                for (Team team : this.teams) {
                    for (Volume teamVolume : team.getSpawnVolumes().values()) {
                        teamVolume.resetBlocks();
                    }
                    if (team.getTeamFlag() != null) {
                        team.getFlagVolume().resetBlocks();
                    }
                }

                for (Monument monument : this.monuments) {
                    monument.getVolume().resetBlocks();
                }

                for (CapturePoint cp : this.capturePoints) {
                    cp.getVolume().resetBlocks();
                }

                for (Bomb bomb : this.bombs) {
                    bomb.getVolume().resetBlocks();
                }

                for (Cake cake : this.cakes) {
                    cake.getVolume().resetBlocks();
                }
            }

            this.volume.saveBlocks();
            if (clearArtifacts) {
                this.initializeZone(); // bring back stuff
            }
            return this.volume.size();
        }
        return 0;
    }

    /**
     * Goes back to the saved state of the warzone (resets only block types, not physics). Also teleports all players back to their respective spawns.
     */
    public void initializeZone() {
        this.initializeZone(null);
    }

    public void initializeZone(Player respawnExempted) {
        if (this.ready() && this.volume.isSaved()) {
            // everyone back to team spawn with full health
            for (Team team : this.teams) {
                for (WarPlayer warPlayer : team.getPlayers()) {
                    if (respawnExempted != null && warPlayer.getUniqueId().equals(respawnExempted.getUniqueId())) {
                        continue;
                    }
                    if (this.getReallyDeadFighters().contains(warPlayer.getUniqueId())) {
                        continue;
                    }
                    this.respawnPlayer(warPlayer.getPlayer());
                }
                team.setRemainingLives(team.getTeamConfig().resolveInt(TeamConfig.LIFEPOOL));
                team.initializeTeamSpawns();
                if (team.getTeamFlag() != null) {
                    team.setTeamFlag(team.getTeamFlag());
                }
            }

            this.initZone();

            this.resetPortals();
        }

        // Don't forget to reset these to false, or we won't be able to score or empty lifepools anymore
        this.isReinitializing = false;
        this.isEndOfGame = false;
    }

    public void initializeZoneAsJob(Player respawnExempted) {
        InitZoneJob job = new InitZoneJob(this, respawnExempted);
        War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, job);
    }

    public void initializeZoneAsJob() {
        InitZoneJob job = new InitZoneJob(this);
        War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, job);
    }

    private void initZone() {
        // reset monuments
        for (Monument monument : this.monuments) {
            monument.getVolume().resetBlocks();
            monument.addMonumentBlocks();
        }

        // reset capture points
        for (CapturePoint cp : this.capturePoints) {
            cp.getVolume().resetBlocks();
            cp.reset();
        }

        // reset bombs
        for (Bomb bomb : this.bombs) {
            bomb.getVolume().resetBlocks();
            bomb.addBombBlocks();
        }

        // reset cakes
        for (Cake cake : this.cakes) {
            cake.getVolume().resetBlocks();
            cake.addCakeBlocks();
        }

        // reset portals
        for (ZonePortal portal : this.portals) {
            portal.reset();
        }

        this.flagThieves.clear();
        this.bombThieves.clear();
        this.cakeThieves.clear();
        this.reallyDeadFighters.clear();

        //get them config (here be crazy grinning's!)
        int pvpready = warzoneConfig.getInt(WarzoneConfig.PREPTIME);

        if (pvpready != 0) { //if it is equalz to zeroz then dinosaurs will take over the earth
            this.pvpReady = false;
            ZoneTimeJob timer = new ZoneTimeJob(this);
            War.war.getServer().getScheduler().runTaskLater(War.war, timer, pvpready * 20);
        }

        // nom drops
        for (Entity entity : (this.getWorld().getEntities())) {
            if (!(entity instanceof Item)) {
                continue;
            }

            // validate position
            if (!this.getVolume().contains(entity.getLocation())) {
                continue;
            }

            // omnomnomnom
            entity.remove();
        }
    }

    public void endRound() {

    }

    public void respawnPlayer(Player player) {
        // Teleport the player back to spawn
        player.setVelocity(new Vector());

        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        this.handleRespawn(player, warPlayer.getTeam().getRandomSpawn());
    }

    public void respawnPlayer(PlayerMoveEvent event, Player player) {
        // Teleport the player back to spawn
        player.setVelocity(new Vector());

        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        event.setTo(warPlayer.getTeam().getRandomSpawn());
        this.handleRespawn(player, event.getTo());
    }

    public boolean isRespawning(WarPlayer p) {
        return respawn.contains(p);
    }

    private void handleRespawn(Player player, Location location) {
        TeleportToSpawnTimer timer = new TeleportToSpawnTimer(player, location);
        BukkitTask teleportTask = timer.runTaskTimer(War.war, 0, 1);
        War.war.getServer().getScheduler().runTaskLater(War.war, teleportTask::cancel, 10);

        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team team = warPlayer.getTeam();

        // first, wipe inventory to disable attribute modifications
        warPlayer.preventItemHacking();
        player.getInventory().clear();

        // clear potion effects
        PotionEffectHelper.clearPotionEffects(player);

        // Fill hp
        player.setRemainingAir(player.getMaximumAir());
        AttributeInstance ai = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        for (AttributeModifier mod : ai.getModifiers()) {
            ai.removeModifier(mod);
        }
        ai.setBaseValue(20.0);
        player.setHealth(ai.getValue());
        player.setFoodLevel(20);
        player.setSaturation(team.getTeamConfig().resolveInt(TeamConfig.SATURATION));
        player.setExhaustion(0);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5, 255));
        Runnable antiFireAction = () -> {
            // Stop fire here, since doing it in the same tick as death doesn't extinguish it
            player.setFireTicks(0);
        };
        // ughhhhh bukkit
        War.war.getServer().getScheduler().runTaskLater(War.war, antiFireAction, 1L);
        War.war.getServer().getScheduler().runTaskLater(War.war, antiFireAction, 2L);
        War.war.getServer().getScheduler().runTaskLater(War.war, antiFireAction, 3L);
        War.war.getServer().getScheduler().runTaskLater(War.war, antiFireAction, 4L);
        War.war.getServer().getScheduler().runTaskLater(War.war, antiFireAction, 5L);

        player.setLevel(0);
        player.setExp(0);
        player.setAllowFlight(false);
        player.setFlying(false);

        // Restore the player to maximum mana
        int maxMana = MagicSpells.getManaHandler().getMaxMana(player);
        MagicSpells.getManaHandler().setMana(player, maxMana, ManaChangeReason.POTION);

        if (player.getGameMode() != GameMode.SURVIVAL) {
            // Players are always in survival mode in warzones
            player.setGameMode(GameMode.SURVIVAL);
        }

        String potionEffect = team.getTeamConfig().resolveString(TeamConfig.APPLYPOTION);
        if (!potionEffect.isEmpty()) {
            PotionEffect effect = War.war.getPotionEffect(potionEffect);
            if (effect != null) {
                player.addPotionEffect(effect);
            } else {
                War.war.getLogger().log(Level.WARNING, "Failed to apply potion effect {0} in warzone {1}.", new Object[]{potionEffect, name});
            }
        }

        int respawnTime = team.getTeamConfig().resolveInt(TeamConfig.RESPAWNTIMER);
        int respawnTimeTicks = respawnTime * 20;
        if (respawnTimeTicks > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, respawnTimeTicks, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, respawnTimeTicks, 200));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, respawnTimeTicks, 255));
            player.sendTitle("", ChatColor.RED + MessageFormat.format(War.war.getString("zone.spawn.timer.title"), respawnTime), 1, respawnTimeTicks, 10);
        }

        if (warPlayer.getLoadoutSelection() == null) {
            warPlayer.setLoadoutSelection(new LoadoutSelection(true, "Sorcerer"));
        } else {
            warPlayer.getLoadoutSelection().setStillInSpawn(true);
        }

        War.war.getKillstreakReward().getAirstrikePlayers().remove(player.getName());

        final LoadoutResetJob job = new LoadoutResetJob(warPlayer);
        if (team.getTeamConfig().resolveInt(TeamConfig.RESPAWNTIMER) == 0) {
            job.run();
        } else {
            // "Respawn" Timer - player will not be able to leave spawn for a few seconds
            respawn.add(warPlayer);

            War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, () -> {
                respawn.remove(warPlayer);
                War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, job);
            }, team.getTeamConfig().resolveInt(TeamConfig.RESPAWNTIMER) * 20L); // 20 ticks = 1 second
        }
    }

    private void resetInventory(Team team, Player player, Loadout loadout) {
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
        if (this.getWarzoneConfig().getBoolean(WarzoneConfig.BLOCKHEADS)) {
            playerInv.setHelmet(team.getKind().getHat());
        }
    }

    public boolean isMonumentCenterBlock(Block block) {
        for (Monument monument : this.monuments) {
            int x = monument.getLocation().getBlockX();
            int y = monument.getLocation().getBlockY() + 1;
            int z = monument.getLocation().getBlockZ();
            if (x == block.getX() && y == block.getY() && z == block.getZ()) {
                return true;
            }
        }
        return false;
    }

    public Monument getMonumentFromCenterBlock(Block block) {
        for (Monument monument : this.monuments) {
            int x = monument.getLocation().getBlockX();
            int y = monument.getLocation().getBlockY() + 1;
            int z = monument.getLocation().getBlockZ();
            if (x == block.getX() && y == block.getY() && z == block.getZ()) {
                return monument;
            }
        }
        return null;
    }

    public boolean nearAnyOwnedMonument(Location to, Team team) {
        for (Monument monument : this.monuments) {
            if (monument.isNear(to) && monument.isOwner(team)) {
                return true;
            }
        }
        return false;
    }

    public Set<Monument> getMonuments() {
        return this.monuments;
    }

    public boolean hasMonument(String monumentName) {
        for (Monument monument : this.monuments) {
            if (monument.getName().startsWith(monumentName)) {
                return true;
            }
        }
        return false;
    }

    public Monument getMonument(String monumentName) {
        for (Monument monument : this.monuments) {
            if (monument.getName().startsWith(monumentName)) {
                return monument;
            }
        }
        return null;
    }

    public boolean hasCapturePoint(String capturePointName) {
        return this.getCapturePoint(capturePointName) != null;
    }

    public CapturePoint getCapturePoint(String capturePointName) {
        for (CapturePoint cp : this.capturePoints) {
            if (cp.getName().startsWith(capturePointName)) {
                return cp;
            }
        }
        return null;
    }

    public boolean hasBomb(String bombName) {
        for (Bomb bomb : this.bombs) {
            if (bomb.getName().equals(bombName)) {
                return true;
            }
        }
        return false;
    }

    public Bomb getBomb(String bombName) {
        for (Bomb bomb : this.bombs) {
            if (bomb.getName().startsWith(bombName)) {
                return bomb;
            }
        }
        return null;
    }

    public boolean hasCake(String cakeName) {
        for (Cake cake : this.cakes) {
            if (cake.getName().equals(cakeName)) {
                return true;
            }
        }
        return false;
    }

    public Cake getCake(String cakeName) {
        for (Cake cake : this.cakes) {
            if (cake.getName().startsWith(cakeName)) {
                return cake;
            }
        }
        return null;
    }

    public boolean isImportantBlock(Block block) {
        if (block == null) {
            return false;
        }
        if (this.ready()) {
            for (Monument m : this.monuments) {
                if (m.getVolume().contains(block)) {
                    return true;
                }
            }
            for (CapturePoint cp : this.capturePoints) {
                if (cp.getVolume().contains(block)) {
                    return true;
                }
            }
            for (Bomb b : this.bombs) {
                if (b.getVolume().contains(block)) {
                    return true;
                }
            }
            for (Cake c : this.cakes) {
                if (c.getVolume().contains(block)) {
                    return true;
                }
            }
            for (Team t : this.teams) {
                for (Volume tVolume : t.getSpawnVolumes().values()) {
                    if (tVolume.contains(block)) {
                        return true;
                    }
                }
                if (t.getFlagVolume() != null && t.getFlagVolume().contains(block)) {
                    return true;
                }
            }
            return this.volume.isWallBlock(block);
        }
        return false;
    }

    public World getWorld() {

        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public ZoneVolume getVolume() {
        return this.volume;
    }

    public void setVolume(ZoneVolume zoneVolume) {
        this.volume = zoneVolume;
    }

    public Team getTeamByKind(TeamKind kind) {
        for (Team t : this.teams) {
            if (t.getKind() == kind) {
                return t;
            }
        }
        return null;
    }

    public boolean isNearWall(Location latestPlayerLocation) {
        if (this.volume.hasTwoCorners()) {
            if (Math.abs(this.volume.getSoutheastZ() - latestPlayerLocation.getBlockZ()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getSoutheastX() && latestPlayerLocation.getBlockX() >= this.volume.getNorthwestX() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
                return true; // near east wall
            } else if (Math.abs(this.volume.getSoutheastX() - latestPlayerLocation.getBlockX()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockZ() <= this.volume.getNorthwestZ() && latestPlayerLocation.getBlockZ() >= this.volume.getSoutheastZ() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
                return true; // near south wall
            } else if (Math.abs(this.volume.getNorthwestX() - latestPlayerLocation.getBlockX()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockZ() <= this.volume.getNorthwestZ() && latestPlayerLocation.getBlockZ() >= this.volume.getSoutheastZ() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
                return true; // near north wall
            } else if (Math.abs(this.volume.getNorthwestZ() - latestPlayerLocation.getBlockZ()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getSoutheastX() && latestPlayerLocation.getBlockX() >= this.volume.getNorthwestX() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
                return true; // near west wall
            } else if (Math.abs(this.volume.getMaxY() - latestPlayerLocation.getBlockY()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getMaxX() && latestPlayerLocation.getBlockX() >= this.volume.getMinX() && latestPlayerLocation.getBlockZ() <= this.volume.getMaxZ() && latestPlayerLocation.getBlockZ() >= this.volume.getMinZ()) {
                return true; // near up wall
            } else if (Math.abs(this.volume.getMinY() - latestPlayerLocation.getBlockY()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getMaxX() && latestPlayerLocation.getBlockX() >= this.volume.getMinX() && latestPlayerLocation.getBlockZ() <= this.volume.getMaxZ() && latestPlayerLocation.getBlockZ() >= this.volume.getMinZ()) {
                return true; // near down wall
            }
        }
        return false;
    }

    public List<Block> getNearestWallBlocks(Location latestPlayerLocation) {
        List<Block> nearestWallBlocks = new ArrayList<>();
        if (Math.abs(this.volume.getSoutheastZ() - latestPlayerLocation.getBlockZ()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getSoutheastX() && latestPlayerLocation.getBlockX() >= this.volume.getNorthwestX() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near east wall
            Block eastWallBlock = this.world.getBlockAt(latestPlayerLocation.getBlockX() + 1, latestPlayerLocation.getBlockY() + 1, this.volume.getSoutheastZ());
            nearestWallBlocks.add(eastWallBlock);
        }

        if (Math.abs(this.volume.getSoutheastX() - latestPlayerLocation.getBlockX()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockZ() <= this.volume.getNorthwestZ() && latestPlayerLocation.getBlockZ() >= this.volume.getSoutheastZ() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near south wall
            Block southWallBlock = this.world.getBlockAt(this.volume.getSoutheastX(), latestPlayerLocation.getBlockY() + 1, latestPlayerLocation.getBlockZ());
            nearestWallBlocks.add(southWallBlock);
        }

        if (Math.abs(this.volume.getNorthwestX() - latestPlayerLocation.getBlockX()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockZ() <= this.volume.getNorthwestZ() && latestPlayerLocation.getBlockZ() >= this.volume.getSoutheastZ() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near north wall
            Block northWallBlock = this.world.getBlockAt(this.volume.getNorthwestX(), latestPlayerLocation.getBlockY() + 1, latestPlayerLocation.getBlockZ());
            nearestWallBlocks.add(northWallBlock);
        }

        if (Math.abs(this.volume.getNorthwestZ() - latestPlayerLocation.getBlockZ()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getSoutheastX() && latestPlayerLocation.getBlockX() >= this.volume.getNorthwestX() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near west wall
            Block westWallBlock = this.world.getBlockAt(latestPlayerLocation.getBlockX(), latestPlayerLocation.getBlockY() + 1, this.volume.getNorthwestZ());
            nearestWallBlocks.add(westWallBlock);
        }

        if (Math.abs(this.volume.getMaxY() - latestPlayerLocation.getBlockY()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getMaxX() && latestPlayerLocation.getBlockX() >= this.volume.getMinX() && latestPlayerLocation.getBlockZ() <= this.volume.getMaxZ() && latestPlayerLocation.getBlockZ() >= this.volume.getMinZ()) {
            // near up wall
            Block upWallBlock = this.world.getBlockAt(latestPlayerLocation.getBlockX(), this.volume.getMaxY(), latestPlayerLocation.getBlockZ());
            nearestWallBlocks.add(upWallBlock);
        }

        if (Math.abs(this.volume.getMinY() - latestPlayerLocation.getBlockY()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getMaxX() && latestPlayerLocation.getBlockX() >= this.volume.getMinX() && latestPlayerLocation.getBlockZ() <= this.volume.getMaxZ() && latestPlayerLocation.getBlockZ() >= this.volume.getMinZ()) {
            // near down wall
            Block downWallBlock = this.world.getBlockAt(latestPlayerLocation.getBlockX(), this.volume.getMinY(), latestPlayerLocation.getBlockZ());
            nearestWallBlocks.add(downWallBlock);
        }
        return nearestWallBlocks;
        // note: y + 1 to line up 3 sided square with player eyes
    }

    public List<BlockFace> getNearestWalls(Location latestPlayerLocation) {
        List<BlockFace> walls = new ArrayList<>();
        if (Math.abs(this.volume.getSoutheastZ() - latestPlayerLocation.getBlockZ()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getSoutheastX() && latestPlayerLocation.getBlockX() >= this.volume.getNorthwestX() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near east wall
            walls.add(Direction.EAST());
        }

        if (Math.abs(this.volume.getSoutheastX() - latestPlayerLocation.getBlockX()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockZ() <= this.volume.getNorthwestZ() && latestPlayerLocation.getBlockZ() >= this.volume.getSoutheastZ() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near south wall
            walls.add(Direction.SOUTH());
        }

        if (Math.abs(this.volume.getNorthwestX() - latestPlayerLocation.getBlockX()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockZ() <= this.volume.getNorthwestZ() && latestPlayerLocation.getBlockZ() >= this.volume.getSoutheastZ() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near north wall
            walls.add(Direction.NORTH());
        }

        if (Math.abs(this.volume.getNorthwestZ() - latestPlayerLocation.getBlockZ()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getSoutheastX() && latestPlayerLocation.getBlockX() >= this.volume.getNorthwestX() && latestPlayerLocation.getBlockY() >= this.volume.getMinY() && latestPlayerLocation.getBlockY() <= this.volume.getMaxY()) {
            // near west wall
            walls.add(Direction.WEST());
        }

        if (Math.abs(this.volume.getMaxY() - latestPlayerLocation.getBlockY()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getMaxX() && latestPlayerLocation.getBlockX() >= this.volume.getMinX() && latestPlayerLocation.getBlockZ() <= this.volume.getMaxZ() && latestPlayerLocation.getBlockZ() >= this.volume.getMinZ()) {
            // near up wall
            walls.add(BlockFace.UP);
        }

        if (Math.abs(this.volume.getMinY() - latestPlayerLocation.getBlockY()) < this.minSafeDistanceFromWall && latestPlayerLocation.getBlockX() <= this.volume.getMaxX() && latestPlayerLocation.getBlockX() >= this.volume.getMinX() && latestPlayerLocation.getBlockZ() <= this.volume.getMaxZ() && latestPlayerLocation.getBlockZ() >= this.volume.getMinZ()) {
            // near down wall
            walls.add(BlockFace.DOWN);
        }
        return walls;
    }

    public ZoneWallGuard getPlayerZoneWallGuard(String name, BlockFace wall) {
        for (ZoneWallGuard guard : this.zoneWallGuards) {
            if (guard.getPlayer().getName().equals(name) && wall == guard.getWall()) {
                return guard;
            }
        }
        return null;
    }

    public boolean protectZoneWallAgainstPlayer(Player player) {
        List<BlockFace> nearestWalls = this.getNearestWalls(player.getLocation());
        boolean protecting = false;
        for (BlockFace wall : nearestWalls) {
            ZoneWallGuard guard = this.getPlayerZoneWallGuard(player.getName(), wall);
            if (guard != null) {
                // already protected, need to move the guard
                guard.updatePlayerPosition(player.getLocation());
            } else {
                // new guard
                guard = new ZoneWallGuard(player, War.war, this, wall);
                this.zoneWallGuards.add(guard);
            }
            protecting = true;
        }
        return protecting;
    }

    public void dropZoneWallGuardIfAny(Player player) {
        List<ZoneWallGuard> playerGuards = new ArrayList<>();
        for (ZoneWallGuard guard : this.zoneWallGuards) {
            if (guard.getPlayer().getName().equals(player.getName())) {
                playerGuards.add(guard);
                guard.deactivate();
            }
        }
        // now remove those zone guards
        for (ZoneWallGuard playerGuard : playerGuards) {
            this.zoneWallGuards.remove(playerGuard);
        }
        playerGuards.clear();
    }

    public Team autoAssign(Player player) {
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        if (warPlayer.getTeam() != null) {
            War.war.badMsg(player, "You are already in a warzone");
            return null;
        }

        int lowest = Integer.MAX_VALUE;
        Team toAssign = null;
        for (Team team : this.teams) {
            if (War.war.canPlayWar(player, team)) {
                int numPlayers = team.getPlayers().size();
                if (numPlayers < lowest) {
                    toAssign = team;
                    lowest = numPlayers;
                }
            }
        }
        if (toAssign != null) {
            this.assign(player, toAssign);
        }
        return toAssign;
    }

    /**
     * Assign a player to a specific team.
     *
     * @param player Player to assign to team.
     * @param team Team to add the player to.
     * @return false if player does not have permission to join this team.
     */
    public boolean assign(Player player, Team team) {
        if (!War.war.canPlayWar(player, team)) {
            War.war.badMsg(player, "join.permission.single");
            return false;
        }
        if (player.getWorld() != this.getWorld()) {
            player.teleport(this.getWorld().getSpawnLocation());
        }
        PermissionAttachment attachment = player.addAttachment(War.war);
        this.attachments.put(player.getUniqueId(), attachment);
        attachment.setPermission("war.playing", true);
        attachment.setPermission("war.playing." + this.getName().toLowerCase(), true);
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        warPlayer.setTeam(team);
        warPlayer.setZone(this);
        team.addPlayer(warPlayer);
        team.resetSign();

        this.getReallyDeadFighters().remove(player.getUniqueId());
        warPlayer.savePlayerState();
        War.war.msg(player, "join.inventorystored");
        this.respawnPlayer(player);
        this.broadcast("join.broadcast", player.getName(), team.getKind().getFormattedName());
        this.resetPortals();
        return true;
    }

    private void dropItems(Location location, ItemStack[] items) {
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            location.getWorld().dropItem(location, item);
        }
    }

    /**
     * Send death messages and process other records before passing off the death to the {@link #handleDeath(Player)}  method.
     *
     * @param attacker Player who killed the defender
     * @param defender Player who was killed
     * @param damager Entity who caused the damage. Usually an arrow. Used for specific death messages. Can be null.
     */
    public void handleKill(Player attacker, Player defender, Entity damager) {
        WarPlayer warAttacker = WarPlayer.getPlayer(attacker.getUniqueId());
        WarPlayer warDefender = WarPlayer.getPlayer(defender.getUniqueId());
        Team attackerTeam = warAttacker.getTeam();
        Team defenderTeam = warDefender.getTeam();

        if (this.getWarzoneConfig().getBoolean(WarzoneConfig.DEATHMESSAGES)) {
            String attackerString = attackerTeam.getKind().getColor() + attacker.getName();
            String defenderString = defenderTeam.getKind().getColor() + defender.getName();
            ItemStack weapon = attacker.getInventory().getItemInMainHand(); // Not the right way to do this, as they could kill with their other hand, but whatever
            Material killerWeapon = weapon.getType();
            String weaponString = killerWeapon.toString();
            if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
                weaponString = weapon.getItemMeta().getDisplayName() + ChatColor.WHITE;
            }
            if (killerWeapon == Material.AIR) {
                weaponString = War.war.getString("pvp.kill.weapon.hand");
            } else if (killerWeapon == Material.BOW || damager instanceof Arrow) {
                int rand = killSeed.nextInt(3);
                if (rand == 0) {
                    weaponString = War.war.getString("pvp.kill.weapon.bow");
                } else {
                    weaponString = War.war.getString("pvp.kill.weapon.aim");
                }
            } else if (damager instanceof Projectile) {
                weaponString = War.war.getString("pvp.kill.weapon.aim");
            }
            String adjectiveString = War.war.getDeadlyAdjectives().isEmpty() ? "" : War.war.getDeadlyAdjectives().get(this.killSeed.nextInt(War.war.getDeadlyAdjectives().size()));
            String verbString = War.war.getKillerVerbs().isEmpty() ? "" : War.war.getKillerVerbs().get(this.killSeed.nextInt(War.war.getKillerVerbs().size()));
            this.broadcast("pvp.kill.format", attackerString + ChatColor.WHITE, adjectiveString, weaponString.toLowerCase().replace('_', ' '), verbString, defenderString);
        }
        warAttacker.addKill();
        this.addKillDeathRecord(attacker, 1, 0);
        this.addKillDeathRecord(defender, 0, 1);
        if (attackerTeam.getTeamConfig().resolveBoolean(TeamConfig.XPKILLMETER)) {
            attacker.setLevel(warAttacker.getKills());
        }
        if (attackerTeam.getTeamConfig().resolveBoolean(TeamConfig.KILLSTREAK)) {
            War.war.getKillstreakReward().rewardPlayer(attacker, warAttacker.getKills());
        }
        if (defenderTeam.getTeamConfig().resolveBoolean(TeamConfig.INVENTORYDROP)) {
            dropItems(defender.getLocation(), defender.getInventory().getContents());
            dropItems(defender.getLocation(), defender.getInventory().getArmorContents());
        }
        this.handleDeath(defender);
    }

    /**
     * Handle death messages before passing to {@link #handleDeath(Player)} for post-processing. It's like {@link #handleKill(Player, Player, Entity)}, but only for suicides.
     *
     * @param player WarPlayer who killed himself
     */
    public void handleSuicide(Player player) {
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        if (this.getWarzoneConfig().getBoolean(WarzoneConfig.DEATHMESSAGES)) {
            String defenderString = warPlayer.getTeam().getKind().getColor() + player.getName() + ChatColor.WHITE;
            this.broadcast("pvp.kill.self", defenderString);
        }
        this.handleDeath(player);
    }

    /**
     * Handle a player killed naturally (like by a dispenser or explosion).
     *
     * @param player Player killed
     * @param event Event causing damage
     */
    public void handleNaturalKill(Player player, EntityDamageEvent event) {
        if (this.getWarzoneConfig().getBoolean(WarzoneConfig.DEATHMESSAGES)) {
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            String defenderString = warPlayer.getTeam().getKind().getColor() + player.getName() + ChatColor.WHITE;
            if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof TNTPrimed) {
                this.broadcast("pvp.death.explosion", defenderString + ChatColor.WHITE);
            } else if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.LIGHTNING) {
                this.broadcast("pvp.death.fire", defenderString);
            } else if (event.getCause() == DamageCause.DROWNING) {
                this.broadcast("pvp.death.drown", defenderString);
            } else if (event.getCause() == DamageCause.FALL) {
                this.broadcast("pvp.death.fall", defenderString);
            } else {
                this.broadcast("pvp.death.other", defenderString);
            }
        }
        this.handleDeath(player);
    }

    /**
     * Cleanup after a player who has died. This decrements the team's remaining lifepool, drops stolen flags, and respawns the player. It also handles team lose and score cap conditions. This method is synchronized to prevent concurrent battle resets.
     *
     * @param player Player who died
     */
    public synchronized void handleDeath(Player player) {
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team playerTeam = warPlayer.getTeam();

        Validate.notNull(playerTeam, "Can't find team for dead player " + player.getName());
        if (this.getWarzoneConfig().getBoolean(WarzoneConfig.REALDEATHS)) {
            this.getReallyDeadFighters().add(player.getUniqueId());
        } else {
            this.respawnPlayer(player);
        }
        if (playerTeam.getRemainingLives() <= 0) {
            handleTeamLoss(playerTeam, warPlayer);
        } else {
            this.dropAllStolenObjects(warPlayer, false);
            playerTeam.setRemainingLives(playerTeam.getRemainingLives() - 1);
            // Lifepool empty warning
            if (playerTeam.getRemainingLives() == 0) {
                this.broadcast("zone.lifepool.empty", playerTeam.getName());
            }
        }
        playerTeam.resetSign();
    }

    private void handleTeamLoss(Team losingTeam, WarPlayer warPlayer) {
        Player player = warPlayer.getPlayer();

        StringBuilder teamScores = new StringBuilder();
        List<Team> winningTeams = new ArrayList<>(teams.size());
        for (Team team : this.teams) {
            if (team.getPlayers().isEmpty()) {
                continue;
            }
            for (WarPlayer wp : team.getPlayers()) {
                wp.resetKills();
            }
            if (team != losingTeam) {
                team.addPoint();
                team.resetSign();
                winningTeams.add(team);
            }
            teamScores.append(String.format("\n%s (%d/%d) ", team.getName(), team.getPoints(), team.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)));
            team.sendAchievement("Round over! " + losingTeam.getKind().getFormattedName(), "ran out of lives.", losingTeam.getKind().getBlockHead(), 10000);
        }
        this.broadcast("zone.battle.end", losingTeam.getName(), player.getName());
        WarBattleWinEvent event1 = new WarBattleWinEvent(this, winningTeams);
        War.war.getServer().getPluginManager().callEvent(event1);
        if (!teamScores.toString().isEmpty()) {
            this.broadcast("zone.battle.newscores", teamScores.toString());
        }
        if (War.war.getMysqlConfig().isEnabled() && War.war.getMysqlConfig().isLoggingEnabled()) {
            LogKillsDeathsJob logKillsDeathsJob = new LogKillsDeathsJob(ImmutableList.copyOf(this.getKillsDeathsTracker()));
            logKillsDeathsJob.runTaskAsynchronously(War.war);
        }

        this.getKillsDeathsTracker().clear();
        if (!detectScoreCap()) {
            this.broadcast("zone.battle.reset");
            if (this.getWarzoneConfig().getBoolean(WarzoneConfig.RESETBLOCKS)) {
                this.reinitialize();
            } else {
                this.initializeZone();
            }
        }
    }

    /**
     * Check if a team has achieved max score "score cap".
     *
     * @return true if team has achieved max score, false otherwise.
     */
    public boolean detectScoreCap() {
        StringBuilder winnersStr = new StringBuilder();
        for (Team team : this.teams) {
            if (team.getPoints() >= team.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)) {
                winnersStr.append(team.getName()).append(' ');
            }
        }
        if (!winnersStr.toString().isEmpty()) {
            this.handleScoreCapReached(winnersStr.toString());
        }
        return !winnersStr.toString().isEmpty();
    }

    public void reinitialize() {
        this.isReinitializing = true;
        this.getVolume().resetBlocksAsJob();
    }

    public void handlePlayerLeave(Player player, boolean removeFromTeam) {
        this.handlePlayerLeave(player);
    }

    private void handlePlayerLeave(Player player) {
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team playerTeam = warPlayer.getTeam();
        if (playerTeam != null) {
            playerTeam.removePlayer(warPlayer);
            this.broadcast("leave.broadcast", playerTeam.getKind().getColor() + player.getName() + ChatColor.WHITE);
            playerTeam.resetSign();
            player.removeAttachment(this.attachments.remove(player.getUniqueId()));
            if (this.getPlayerCount() == 0 && this.getWarzoneConfig().getBoolean(WarzoneConfig.RESETONEMPTY)) {
                // reset the zone for a new game when the last player leaves
                for (Team team : this.getTeams()) {
                    team.resetPoints();
                    team.setRemainingLives(team.getTeamConfig().resolveInt(TeamConfig.LIFEPOOL));
                }
                if (!this.isReinitializing()) {
                    this.reinitialize();
                    War.war.getLogger().log(Level.INFO, "Last player left warzone {0}. Warzone blocks resetting automatically...", new Object[]{this.getName()});
                }
            }
            this.autoTeamBalance();
            this.resetPortals();

            WarPlayerLeaveEvent event1 = new WarPlayerLeaveEvent(player.getName());
            War.war.getServer().getPluginManager().callEvent(event1);
        }
    }

    /**
     * Moves players from team to team if the player size delta is greater than or equal to 2. Only works for autoassign zones.
     */
    private void autoTeamBalance() {
        if (!this.getWarzoneConfig().getBoolean(WarzoneConfig.AUTOASSIGN)) {
            return;
        }
        boolean rerun = false;
        for (Team team1 : this.teams) {
            for (Team team2 : this.teams) {
                if (team1 == team2) {
                    continue;
                }
                int t1p = team1.getPlayers().size();
                int t2p = team2.getPlayers().size();
                if (t1p - t2p >= 2) {
                    WarPlayer eject = team1.getPlayers().get(killSeed.nextInt(t1p));
                    team1.removePlayer(eject);
                    team1.resetSign();
                    this.assign(eject.getPlayer(), team2);
                    rerun = true;
                    break;
                } else if (t2p - t1p >= 2) {
                    WarPlayer eject = team2.getPlayers().get(killSeed.nextInt(t2p));
                    team2.removePlayer(eject);
                    team2.resetSign();
                    this.assign(eject.getPlayer(), team1);
                    rerun = true;
                    break;
                }
            }
        }
        if (rerun) {
            this.autoTeamBalance();
        }
    }

    public boolean isEnemyTeamFlagBlock(Team playerTeam, Block block) {
        for (Team team : this.teams) {
            if (!team.getName().equals(playerTeam.getName()) && team.isTeamFlagBlock(block)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFlagBlock(Block block) {
        for (Team team : this.teams) {
            if (team.isTeamFlagBlock(block)) {
                return true;
            }
        }
        return false;
    }

    public Team getTeamForFlagBlock(Block block) {
        for (Team team : this.teams) {
            if (team.isTeamFlagBlock(block)) {
                return team;
            }
        }
        return null;
    }

    public boolean isBombBlock(Block block) {
        for (Bomb bomb : this.bombs) {
            if (bomb.isBombBlock(block.getLocation())) {
                return true;
            }
        }
        return false;
    }

    public Bomb getBombForBlock(Block block) {
        for (Bomb bomb : this.bombs) {
            if (bomb.isBombBlock(block.getLocation())) {
                return bomb;
            }
        }
        return null;
    }

    public boolean isCakeBlock(Block block) {
        for (Cake cake : this.cakes) {
            if (cake.isCakeBlock(block.getLocation())) {
                return true;
            }
        }
        return false;
    }

    public Cake getCakeForBlock(Block block) {
        for (Cake cake : this.cakes) {
            if (cake.isCakeBlock(block.getLocation())) {
                return cake;
            }
        }
        return null;
    }

    // Flags
    public void addFlagThief(Team lostFlagTeam, WarPlayer flagThief) {
        this.flagThieves.put(flagThief.getUniqueId(), lostFlagTeam);
        WarPlayerThiefEvent event1 = new WarPlayerThiefEvent(flagThief.getPlayer(), WarPlayerThiefEvent.StolenObject.FLAG);
        War.war.getServer().getPluginManager().callEvent(event1);
    }

    public boolean isFlagThief(WarPlayer suspect) {
        return this.flagThieves.containsKey(suspect.getUniqueId());
    }

    public Team getVictimTeamForFlagThief(WarPlayer thief) {
        return this.flagThieves.get(thief.getUniqueId());
    }

    public void removeFlagThief(WarPlayer thief) {
        this.flagThieves.remove(thief.getUniqueId());
    }

    // Bomb
    public void addBombThief(Bomb bomb, WarPlayer bombThief) {
        this.bombThieves.put(bombThief.getUniqueId(), bomb);
        WarPlayerThiefEvent event1 = new WarPlayerThiefEvent(bombThief.getPlayer(), WarPlayerThiefEvent.StolenObject.BOMB);
        War.war.getServer().getPluginManager().callEvent(event1);
    }

    public boolean isBombThief(WarPlayer suspect) {
        return this.bombThieves.containsKey(suspect.getUniqueId());
    }

    public Bomb getBombForThief(WarPlayer thief) {
        return this.bombThieves.get(thief.getUniqueId());
    }

    // Cake

    public void removeBombThief(WarPlayer thief) {
        this.bombThieves.remove(thief.getUniqueId());
    }

    public void addCakeThief(Cake cake, WarPlayer cakeThief) {
        this.cakeThieves.put(cakeThief.getUniqueId(), cake);
        WarPlayerThiefEvent event1 = new WarPlayerThiefEvent(cakeThief.getPlayer(), WarPlayerThiefEvent.StolenObject.CAKE);
        War.war.getServer().getPluginManager().callEvent(event1);
    }

    public boolean isCakeThief(WarPlayer suspect) {
        return this.cakeThieves.containsKey(suspect.getUniqueId());
    }

    public Cake getCakeForThief(WarPlayer thief) {
        return this.cakeThieves.get(thief.getUniqueId());
    }

    public void removeCakeThief(WarPlayer thief) {
        this.cakeThieves.remove(thief.getUniqueId());
    }

    public void clearThieves() {
        this.flagThieves.clear();
        this.bombThieves.clear();
        this.cakeThieves.clear();
    }

    public boolean isTeamFlagStolen(Team team) {
        return flagThieves.values().contains(team);
    }

    public void handleScoreCapReached(String winnersStr) {
        // Score cap reached. Reset everything.
        this.isEndOfGame = true;
        List<Team> winningTeams = new ArrayList<>(teams.size());
        for (String team : winnersStr.split(" ")) {
            winningTeams.add(this.getTeamByKind(TeamKind.getTeam(team)));
        }
        WarScoreCapEvent event1 = new WarScoreCapEvent(winningTeams);
        War.war.getServer().getPluginManager().callEvent(event1);

        for (Team t : this.getTeams()) {
            String winnersStrAndExtra = "Score cap reached. Game is over! Winning team(s): " + winnersStr;
            winnersStrAndExtra += ". Resetting warzone and your inventory...";
            t.teamcast(winnersStrAndExtra);
            double ecoReward = t.getTeamConfig().resolveDouble(TeamConfig.ECOREWARD);
            boolean doEcoReward = ecoReward != 0 && War.war.getEconomy() != null;
            for (Iterator<WarPlayer> it = t.getPlayers().iterator(); it.hasNext(); ) {
                WarPlayer warPlayer = it.next();
                Player player = warPlayer.getPlayer();

                it.remove(); // Remove player from team first to prevent anti-tp
                t.removePlayer(warPlayer);
                if (winnersStr.contains(t.getName())) {
                    // give reward
                    Reward winReward = t.getInventories().resolveWinReward();
                    if (winReward != null) {
                        winReward.rewardPlayer(player);
                    }
                    if (doEcoReward) {
                        EconomyResponse r;
                        if (ecoReward > 0) {
                            r = War.war.getEconomy().depositPlayer(player.getName(), ecoReward);
                        } else {
                            r = War.war.getEconomy().withdrawPlayer(player.getName(), ecoReward);
                        }
                        if (!r.transactionSuccess()) {
                            War.war.getLogger().log(Level.WARNING, "Failed to reward player {0} ${1}. Error: {2}", new Object[]{player.getName(), ecoReward, r.errorMessage});
                        }
                    }
                } else {
                    Reward lossReward = t.getInventories().resolveLossReward();
                    if (lossReward != null) {
                        lossReward.rewardPlayer(player);
                    }
                }
            }
            t.resetPoints();
            t.getPlayers().clear(); // empty the team
            t.resetSign();
        }
        if (this.getWarzoneConfig().getBoolean(WarzoneConfig.RESETBLOCKS)) {
            this.reinitialize();
        } else {
            this.initializeZone();
        }
    }

    public Location getRallyPoint() {
        return this.rallyPoint;
    }

    public void setRallyPoint(Location location) {
        this.rallyPoint = location;
    }

    public void unload() {
        War.war.log("Unloading zone " + this.getName() + "...", Level.INFO);
        for (Team team : this.getTeams()) {
            for (Iterator<WarPlayer> it = team.getPlayers().iterator(); it.hasNext(); ) {
                WarPlayer warPlayer = it.next();
                it.remove();
                team.removePlayer(warPlayer);
            }
        }
        if (this.getWarzoneConfig().getBoolean(WarzoneConfig.RESETONUNLOAD)) {
            this.getVolume().resetBlocks();
        }
    }

    public boolean isEnoughPlayers() {
        int teamsWithEnough = 0;
        for (Team team : teams) {
            if (team.getPlayers().size() >= this.getWarzoneConfig().getInt(WarzoneConfig.MINPLAYERS)) {
                teamsWithEnough++;
            }
        }
        return teamsWithEnough >= this.getWarzoneConfig().getInt(WarzoneConfig.MINTEAMS);
    }

    public boolean isAuthor(Player player) {
        // if no authors, all zonemakers can edit the zone
        return authors.size() == 0 || authors.contains(player.getName());
    }

    public void addAuthor(String playerName) {
        authors.add(playerName);
    }

    public Set<String> getAuthors() {
        return this.authors;
    }

    public String getAuthorsString() {
        String authors = "";
        for (String author : this.getAuthors()) {
            authors += author + ",";
        }
        return authors;
    }

    public void equipPlayerLoadoutSelection(WarPlayer warPlayer) {
        LoadoutSelection selection = warPlayer.getLoadoutSelection();
        Player player = warPlayer.getPlayer();
        Team team = warPlayer.getTeam();
        if (selection != null && !this.isRespawning(warPlayer)) {
            // Make sure that inventory resets dont occur if player has already tp'ed out (due to game end, or somesuch)
            // - repawn timer + this method is why inventories were getting wiped as players exited the warzone.
            Map<String, Loadout> loadouts = team.getInventories().resolveLoadouts();
            if (loadouts.isEmpty()) {
                // Fix for zones that mistakenly only specify a `first' loadout, but do not add any others.
                warPlayer.resetInventory(null);
                War.war.msg(player, "No classes found");
                return;
            }

            String loadoutName = selection.getSelectedLoadout();
            Loadout loadout = loadouts.get(loadoutName);
            if (loadout == null) {
                warPlayer.resetInventory(null);
                War.war.msg(player, "Class not found");
                return;
            }
            warPlayer.resetInventory(loadout);
        }
    }

    public WarzoneConfigBag getWarzoneConfig() {
        return this.warzoneConfig;
    }

    public TeamConfigBag getTeamDefaultConfig() {
        return this.teamDefaultConfig;
    }

    public InventoryBag getDefaultInventories() {
        return this.defaultInventories;
    }

    public Set<Bomb> getBombs() {
        return bombs;
    }

    public Set<Cake> getCakes() {
        return cakes;
    }

    public Set<CapturePoint> getCapturePoints() {
        return capturePoints;
    }

    public Set<UUID> getReallyDeadFighters() {
        return this.reallyDeadFighters;
    }

    public boolean isEndOfGame() {
        return this.isEndOfGame;
    }

    public boolean isReinitializing() {
        return this.isReinitializing;
    }

//	public Object getGameEndLock() {
//		return gameEndLock;
//	}

    public boolean isOpponentSpawnPeripheryBlock(Team team, Block block) {
        for (Team maybeOpponent : this.getTeams()) {
            if (maybeOpponent != team) {
                for (Volume teamSpawnVolume : maybeOpponent.getSpawnVolumes().values()) {
                    Volume periphery = new Volume(new Location(teamSpawnVolume.getWorld(), teamSpawnVolume.getMinX() - 1, teamSpawnVolume.getMinY() - 1, teamSpawnVolume.getMinZ() - 1), new Location(teamSpawnVolume.getWorld(), teamSpawnVolume.getMaxX() + 1, teamSpawnVolume.getMaxY() + 1, teamSpawnVolume.getMaxZ() + 1));
                    if (periphery.contains(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public WarzoneMaterials getWarzoneMaterials() {
        return warzoneMaterials;
    }

    public void setWarzoneMaterials(WarzoneMaterials warzoneMaterials) {
        this.warzoneMaterials = warzoneMaterials;
    }

    public ScoreboardType getScoreboardType() {
        return scoreboardType;
    }

    /**
     * Sets the TEMPORARY scoreboard type for use in this warzone. This type will NOT be persisted in the Warzone config.
     *
     * @param scoreboardType temporary scoreboard type
     */
    public void setScoreboardType(ScoreboardType scoreboardType) {
        this.scoreboardType = scoreboardType;
    }

    public void addKillDeathRecord(OfflinePlayer player, int kills, int deaths) {
        for (Iterator<KillsDeathsRecord> it = this.killsDeathsTracker.iterator(); it.hasNext(); ) {
            LogKillsDeathsJob.KillsDeathsRecord kdr = it.next();
            if (kdr.getPlayer().equals(player)) {
                kills += kdr.getKills();
                deaths += kdr.getDeaths();
                it.remove();
            }
        }
        LogKillsDeathsJob.KillsDeathsRecord kdr = new LogKillsDeathsJob.KillsDeathsRecord(player, kills, deaths);
        this.killsDeathsTracker.add(kdr);
    }

    public List<LogKillsDeathsJob.KillsDeathsRecord> getKillsDeathsTracker() {
        return killsDeathsTracker;
    }

    /**
     * Send a message to all teams.
     *
     * @param message Message or key to translate.
     */
    public void broadcast(String message) {
        for (Team team : this.teams) {
            team.teamcast(message);
        }
    }

    /**
     * Send a message to all teams.
     *
     * @param message Message or key to translate.
     * @param args Arguments for the formatter.
     */
    public void broadcast(String message, Object... args) {
        for (Team team : this.teams) {
            team.teamcast(message, args);
        }
    }

    /**
     * Get a list of all players in the warzone. The list is immutable. If you need to modify the player list, you must use the per-team lists
     *
     * @return list containing all team players.
     */
    public Set<WarPlayer> getPlayers() {
        Set<WarPlayer> players = new HashSet<>();
        for (Team team : this.teams) {
            players.addAll(team.getPlayers());
        }
        return players;
    }

    /**
     * Get the amount of players in all teams in this warzone.
     *
     * @return total player count
     */
    public int getPlayerCount() {
        int count = 0;
        for (Team team : this.teams) {
            count += team.getPlayers().size();
        }
        return count;
    }

    public int getMaxPlayers() {
        int zoneCap = 0;
        for (Team t : this.getTeams()) {
            zoneCap += t.getTeamConfig().resolveInt(TeamConfig.TEAMSIZE);
        }
        return zoneCap;
    }

    /**
     * Get the amount of players in all teams in this warzone. Same as {@link #getPlayerCount()}, except only checks teams that the specified player has permission to join.
     *
     * @param target Player to check for permissions.
     * @return total player count in teams the player has access to.
     */
    public int getPlayerCount(Permissible target) {
        int playerCount = 0;
        for (Team team : this.teams) {
            if (target.hasPermission(team.getTeamConfig().resolveString(TeamConfig.PERMISSION))) {
                playerCount += team.getPlayers().size();
            }
        }
        return playerCount;
    }

    /**
     * Get the total capacity of all teams in this zone. This should be preferred over {@link TeamConfig#TEAMSIZE} as that can differ per team.
     *
     * @return capacity of all teams in this zone
     */
    public int getTotalCapacity() {
        int capacity = 0;
        for (Team team : this.teams) {
            capacity += team.getTeamConfig().resolveInt(TeamConfig.TEAMSIZE);
        }
        return capacity;
    }

    /**
     * Get the total capacity of all teams in this zone. Same as {@link #getTotalCapacity()}, except only checks teams that the specified player has permission to join.
     *
     * @param target Player to check for permissions.
     * @return capacity of teams the player has access to.
     */
    public int getTotalCapacity(Permissible target) {
        int capacity = 0;
        for (Team team : this.teams) {
            if (target.hasPermission(team.getTeamConfig().resolveString(TeamConfig.PERMISSION))) {
                capacity += team.getTeamConfig().resolveInt(TeamConfig.TEAMSIZE);
            }
        }
        return capacity;
    }

    /**
     * Check if all teams are full.
     *
     * @return true if all teams are full, false otherwise.
     */
    public boolean isFull() {
        return this.getPlayerCount() == this.getTotalCapacity();
    }

    /**
     * Check if all teams are full. Same as {@link #isFull()}, except only checks teams that the specified player has permission to join.
     *
     * @param target Player to check for permissions.
     * @return true if all teams are full, false otherwise.
     */
    public boolean isFull(Permissible target) {
        return this.getPlayerCount(target) == this.getTotalCapacity(target);
    }

    public void dropAllStolenObjects(WarPlayer warPlayer, boolean quiet) {
        Player player = warPlayer.getPlayer();
        if (this.isFlagThief(warPlayer)) {
            Team victimTeam = this.getVictimTeamForFlagThief(warPlayer);

            this.removeFlagThief(warPlayer);

            // Bring back flag of victim team
            victimTeam.getFlagVolume().resetBlocks();
            victimTeam.initializeTeamFlag();

            if (!quiet) {
                this.broadcast("drop.flag.broadcast", warPlayer.getPlayer().getName(), victimTeam.getKind().getColor() + victimTeam.getName() + ChatColor.WHITE);
            }
        } else if (this.isCakeThief(warPlayer)) {
            Cake cake = this.getCakeForThief(warPlayer);

            this.removeCakeThief(warPlayer);

            // Bring back cake
            cake.getVolume().resetBlocks();
            cake.addCakeBlocks();

            if (!quiet) {
                this.broadcast("drop.cake.broadcast", player.getName(), ChatColor.GREEN + cake.getName() + ChatColor.WHITE);
            }
        } else if (this.isBombThief(warPlayer)) {
            Bomb bomb = this.getBombForThief(warPlayer);

            this.removeBombThief(warPlayer);

            // Bring back bomb
            bomb.getVolume().resetBlocks();
            bomb.addBombBlocks();

            if (!quiet) {
                this.broadcast("drop.bomb.broadcast", player.getName(), ChatColor.GREEN + bomb.getName() + ChatColor.WHITE);
            }
        }
    }

    /**
     * Get the proper ending teleport location for players leaving the warzone. <p> Specifically, it gets teleports in this order: <ul> <li>Rally point (if scorecap) <li>Warhub (if autojoin)</ul> </p>
     *
     * @param reason Reason for leaving zone
     */
    public Location getEndTeleport(LeaveCause reason) {
        if (reason.useRallyPoint() && this.getRallyPoint() != null) {
            return this.getRallyPoint();
        }
        return this.getTeleport();
    }

    public Volume loadStructure(String volName, World world) throws SQLException {
        return loadStructure(volName, world, ZoneVolumeMapper.getZoneConnection(volume, name, world));
    }

    public Volume loadStructure(String volName, Connection zoneConnection) throws SQLException {
        return loadStructure(volName, world, zoneConnection);
    }

    public Volume loadStructure(String volName, World world, Connection zoneConnection) throws SQLException {
        Volume volume = new Volume(volName, world);
        if (!containsTable(String.format("structure_%d_corners", volName.hashCode() & Integer.MAX_VALUE), zoneConnection)) {
            volume = VolumeMapper.loadVolume(volName, name, world);
            ZoneVolumeMapper.saveStructure(volume, zoneConnection);
            War.war.getLogger().log(Level.INFO, "Stuffed structure {0} into database for warzone {1}", new Object[]{volName, name});
            return volume;
        }
        ZoneVolumeMapper.loadStructure(volume, zoneConnection);
        return volume;
    }

    private boolean containsTable(String table, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) AS ct FROM sqlite_master WHERE type = ? AND name = ?");
        stmt.setString(1, "table");
        stmt.setString(2, table);
        ResultSet resultSet = stmt.executeQuery();
        try {
            return resultSet.next() && resultSet.getInt("ct") > 0;
        } finally {
            resultSet.close();
            stmt.close();
        }
    }

    public List<ZonePortal> getPortals() {
        return portals;
    }

    public void addPortal(ZonePortal portal) {
        War.war.addPortal(portal);
        for (int i = 0; i < portals.size(); i++) {
            ZonePortal p = portals.get(i);
            if (p.getName().equals(portal.getName())) {
                portals.set(i, portal);
                return;
            }
        }
        portals.add(portal);
    }

    public boolean deletePortal(String portalName) {
        for (Iterator<ZonePortal> it = portals.iterator(); it.hasNext(); ) {
            ZonePortal curr = it.next();
            if (curr.getName().equals(portalName)) {
                curr.getVolume().resetBlocks();
                War.war.removePortal(curr);
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void resetPortals() {
        for (ZonePortal portal : portals) {
            portal.reset();
        }
    }

    public Map<UUID, Team> getFlagThieves() {
        return flagThieves;
    }

    public Map<UUID, Bomb> getBombThieves() {
        return bombThieves;
    }

    public Map<UUID, Cake> getCakeThieves() {
        return cakeThieves;
    }

    /**
     * Check if a player has stolen from a warzone flag, bomb, or cake.
     *
     * @param suspect Player to check.
     * @return true if suspect has stolen a structure.
     */
    public boolean isThief(WarPlayer suspect) {
        return this.isFlagThief(suspect) || this.isBombThief(suspect) || this.isCakeThief(suspect);
    }

    public boolean getPvpReady() {
        return this.pvpReady;
    }

    public void setPvpReady(boolean ready) {
        this.pvpReady = ready;
    }

    public enum LeaveCause {
        COMMAND, DISCONNECT, SCORECAP, RESET;

        public boolean useRallyPoint() {
            return this == SCORECAP;
        }
    }
}
