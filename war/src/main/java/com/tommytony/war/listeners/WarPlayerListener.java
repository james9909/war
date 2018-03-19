package com.tommytony.war.listeners;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.command.ZoneSetter;
import com.tommytony.war.config.FlagReturn;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.WarConfig;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.structure.Bomb;
import com.tommytony.war.structure.Cake;
import com.tommytony.war.structure.ZonePortal;
import com.tommytony.war.utility.Direction;
import com.tommytony.war.utility.LoadoutSelection;
import com.tommytony.war.volume.Volume;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * @author tommytony, Tim Düsterhus
 */
public class WarPlayerListener implements Listener {

    private static final int MINIMUM_TEAM_BLOCKS = 1;
    private java.util.Random random = new java.util.Random();
    private HashMap<String, Location> latestLocations = new HashMap<>();

    /**
     * Correctly removes quitting players from warzones
     *
     * @see PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (War.war.isLoaded()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Warzone zone = warPlayer.getZone();
            if (zone != null) {
                zone.handlePlayerLeave(player, true);
            }

            if (War.war.isWandBearer(player)) {
                War.war.removeWandBearer(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        String autojoinName = War.war.getWarConfig().getString(WarConfig.AUTOJOIN);
        boolean autojoinEnabled = !autojoinName.isEmpty();
        if (autojoinEnabled) { // Won't be able to find warzone if unset
            Warzone autojoinWarzone = Warzone.getZoneByNameExact(autojoinName);
            if (autojoinWarzone == null) {
                War.war.getLogger().log(Level.WARNING, "Failed to find autojoin warzone ''{0}''.", new Object[]{autojoinName});
                return;
            }
            if (autojoinWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED) || autojoinWarzone.isReinitializing()) {
                War.war.badMsg(event.getPlayer(), "join.disabled");
            } else if (!autojoinWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.JOINMIDBATTLE) && autojoinWarzone.isEnoughPlayers()) {
                War.war.badMsg(event.getPlayer(), "join.progress");
            } else if (autojoinWarzone.isFull()) {
                War.war.badMsg(event.getPlayer(), "join.full.all");
            } else if (autojoinWarzone.isFull(event.getPlayer())) {
                War.war.badMsg(event.getPlayer(), "join.permission.all");
            } else { // Player will only ever be autoassigned to a team
                autojoinWarzone.autoAssign(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (War.war.isLoaded()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Team team = warPlayer.getTeam();
            if (team != null) {
                Warzone zone = warPlayer.getZone();
                if (zone.isFlagThief(warPlayer)) {
                    // a flag thief can't drop his flag
                    War.war.badMsg(player, "drop.flag.disabled");
                    event.setCancelled(true);
                } else if (zone.isBombThief(warPlayer)) {
                    // a bomb thief can't drop his bomb
                    War.war.badMsg(player, "drop.bomb.disabled");
                    event.setCancelled(true);
                } else if (zone.isCakeThief(warPlayer)) {
                    // a cake thief can't drop his cake
                    War.war.badMsg(player, "drop.cake.disabled");
                    event.setCancelled(true);
                } else if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.NODROPS)) {
                    War.war.badMsg(player, "drop.item.disabled");
                    event.setCancelled(true);
                } else {
                    Item item = event.getItemDrop();
                    if (item != null) {
                        ItemStack itemStack = item.getItemStack();
                        if (itemStack != null && team.getKind().isTeamItem(itemStack)) {
                            // Can't drop your team's kind block
                            War.war.badMsg(player, "drop.team", team.getName());
                            event.setCancelled(true);
                            return;
                        }

                        if (zone.isNearWall(player.getLocation()) && itemStack != null && !team.getTeamConfig().resolveBoolean(TeamConfig.BORDERDROP)) {
                            War.war.badMsg(player, "drop.item.border");
                            event.setCancelled(true);
                            return;
                        }

                        LoadoutSelection selection = warPlayer.getLoadoutSelection();
                        if (selection != null && selection.isStillInSpawn()) {
                            // still at spawn
                            War.war.badMsg(player, "drop.item.spawn");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            if (War.war.isWandBearer(player)) {
                Item item = event.getItemDrop();
                if (item.getItemStack().getType() == Material.WOOD_SWORD) {
                    String zoneName = War.war.getWandBearerZone(player);
                    War.war.removeWandBearer(player);
                    War.war.msg(player, "drop.wand", zoneName);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        if (War.war.isLoaded()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Team team = warPlayer.getTeam();
            if (team != null) {
                Warzone zone = warPlayer.getZone();

                if (zone.isFlagThief(warPlayer)) {
                    // a flag thief can't pick up anything
                    event.setCancelled(true);
                } else {
                    Item item = event.getItem();
                    if (item != null) {
                        ItemStack itemStack = item.getItemStack();
                        if (itemStack != null && team.getKind().isTeamItem(itemStack) && player.getInventory().containsAtLeast(team.getKind().getBlockHead(), MINIMUM_TEAM_BLOCKS)) {
                            // Can't pick up a second precious block
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if (War.war.isLoaded()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Team team = warPlayer.getTeam();
            if (team != null) {
                String msg = event.getMessage();
                String[] split = msg.split(" ");
                if (!War.war.isWarAdmin(player) && split.length > 0 && split[0].startsWith("/")) {
                    String command = split[0].substring(1);
                    if (!command.equals("war") && !command.equals("class") && !command.equals("zones") && !command.equals("warzones") && !command.equals("zone") && !command.equals("warzone") && !command.equals("teams")
                        && !command.equals("join") && !command.equals("leave") && !command.equals("team") && !command.equals("warhub") && !command.equals("zonemaker")) {
                        // allow white commands
                        for (String whiteCommand : War.war.getCommandWhitelist()) {
                            if (whiteCommand.equals(command)) {
                                return;
                            }
                        }

                        War.war.badMsg(player, "command.disabled");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerKick(final PlayerKickEvent event) {
        if (War.war.isLoaded()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Warzone zone = warPlayer.getZone();

            if (zone != null) {
                // kick player from warzone as well
                zone.handlePlayerLeave(player, true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (War.war.isLoaded()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            if (event.getItem() != null && event.getItem().getType() == Material.WOOD_SWORD && War.war.isWandBearer(player)) {
                String zoneName = War.war.getWandBearerZone(player);
                ZoneSetter setter = new ZoneSetter(player, zoneName);
                switch (event.getAction()) {
                    case LEFT_CLICK_AIR:
                    case RIGHT_CLICK_AIR:
                        War.war.badMsg(player, "wand.toofar");
                        break;
                    case LEFT_CLICK_BLOCK:
                        setter.placeCorner1(event.getClickedBlock());
                        event.setUseItemInHand(Result.ALLOW);
                        break;
                    case RIGHT_CLICK_BLOCK:
                        setter.placeCorner2(event.getClickedBlock());
                        event.setUseItemInHand(Result.ALLOW);
                        break;
                }
            }

            Warzone zone = warPlayer.getZone();
            LoadoutSelection selection = warPlayer.getLoadoutSelection();
            if (zone != null && selection != null && selection.isStillInSpawn()) {
                event.setUseItemInHand(Result.DENY);
                event.setCancelled(true);
                // Replace message with sound to reduce spamminess.
                // Whenever a player dies in the middle of conflict they will
                // likely respawn still trying to use their items to attack
                // another player.
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0);
            }

            if (zone != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.ENDER_CHEST && !zone.getWarzoneConfig()
                .getBoolean(WarzoneConfig.ALLOWENDER)) {
                event.setCancelled(true);
                War.war.badMsg(player, "use.ender");
            }

            Team team = warPlayer.getTeam();
            if (zone != null && team != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE && team.getTeamConfig()
                .resolveBoolean(TeamConfig.XPKILLMETER)) {
                event.setCancelled(true);
                War.war.badMsg(player, "use.enchant");
                if (zone.getAuthors().contains(player.getName())) {
                    War.war.badMsg(player, "use.xpkillmeter");
                }
            }
            if (zone != null && team != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.ANVIL && team.getTeamConfig()
                .resolveBoolean(TeamConfig.XPKILLMETER)) {
                event.setCancelled(true);
                War.war.badMsg(player, "use.anvil");
                if (zone.getAuthors().contains(player.getName())) {
                    War.war.badMsg(player, "use.xpkillmeter");
                }
            }
            if (zone != null && team != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof InventoryHolder && zone.isFlagThief(warPlayer)) {
                event.setCancelled(true);
                War.war.badMsg(player, "drop.flag.disabled");
            }
            if (zone == null && event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.TRAPPED_CHEST)
                && Warzone.getZoneByLocation(event.getClickedBlock().getLocation()) != null && !War.war.isZoneMaker(event.getPlayer())) {
                // prevent opening chests inside a warzone if a player is not a zone maker
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 0);
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Warzone zone = warPlayer.getZone();
            if (zone != null && zone.getWarzoneConfig().getBoolean(WarzoneConfig.SOUPHEALING)) {
                ItemStack item = event.getItem();
                if ((item != null) && (item.getType() == Material.MUSHROOM_SOUP)) {
                    if (player.getHealth() < 20) {
                        player.setHealth(Math.min(20, player.getHealth() + 7));
                        item.setType(Material.BOWL);
                    } else if (player.getFoodLevel() < 20) {
                        player.setFoodLevel(Math.min(20, player.getFoodLevel() + 6));
                        player.setSaturation(player.getSaturation() + 7.2f);
                        item.setType(Material.BOWL);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        Player player = event.getPlayer();
        Location playerLoc = event.getTo(); // Don't call again we need same result.
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team playerTeam = warPlayer.getTeam();
        Warzone playerWarzone = warPlayer.getZone();

        Location previousLocation = latestLocations.get(player.getName());
        if (previousLocation != null && playerLoc.getBlockX() == previousLocation.getBlockX() && playerLoc.getBlockY() == previousLocation.getBlockY() && playerLoc.getBlockZ() == previousLocation
            .getBlockZ() && playerLoc.getWorld() == previousLocation.getWorld()) {
            // we only care when people change location
            return;
        }
        latestLocations.put(player.getName(), playerLoc);

        Warzone locZone = Warzone.getZoneByLocation(playerLoc);

        boolean isMaker = War.war.isZoneMaker(player);

        // Zone walls
        boolean protecting = false;
        if (playerTeam != null) {
            if (playerWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.GLASSWALLS)) {
                protecting = playerWarzone.protectZoneWallAgainstPlayer(player);
            }
        } else {
            Warzone nearbyZone = War.war.zoneOfZoneWallAtProximity(playerLoc);
            if (nearbyZone != null && nearbyZone.getWarzoneConfig().getBoolean(WarzoneConfig.GLASSWALLS) && !isMaker) {
                protecting = nearbyZone.protectZoneWallAgainstPlayer(player);
            }
        }

        if (!protecting) {
            // zone makers still need to delete their walls
            // make sure to delete any wall guards as you leave
            for (Warzone zone : War.war.getWarzones()) {
                zone.dropZoneWallGuardIfAny(player);
            }
        }

        if (playerWarzone != null) {
            // Player belongs to a warzone team but is outside: he snuck out or is at spawn and died
            if (locZone == null && playerTeam != null) {
                List<BlockFace> nearestWalls = playerWarzone.getNearestWalls(playerLoc);
                if (!playerWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.REALDEATHS)) {
                    War.war.badMsg(player, "zone.leavenotice");
                }
                if (nearestWalls != null && nearestWalls.size() > 0) {
                    // First, try to bump the player back in
                    int northSouthMove = 0;
                    int eastWestMove = 0;
                    int upDownMove = 0;
                    int moveDistance = 1;

                    if (nearestWalls.contains(Direction.NORTH())) {
                        // move south
                        northSouthMove += moveDistance;
                    } else if (nearestWalls.contains(Direction.SOUTH())) {
                        // move north
                        northSouthMove -= moveDistance;
                    }

                    if (nearestWalls.contains(Direction.EAST())) {
                        // move west
                        eastWestMove += moveDistance;
                    } else if (nearestWalls.contains(Direction.WEST())) {
                        // move east
                        eastWestMove -= moveDistance;
                    }

                    if (nearestWalls.contains(BlockFace.UP)) {
                        upDownMove -= moveDistance;
                    } else if (nearestWalls.contains(BlockFace.DOWN)) {
                        // fell off the map, back to spawn (still need to drop objects)
                        playerWarzone.dropAllStolenObjects(warPlayer, false);
                        playerWarzone.respawnPlayer(event, player);
                        return;
                    }

                    event.setTo(new Location(playerLoc.getWorld(), playerLoc.getX() + northSouthMove, playerLoc.getY() + upDownMove, playerLoc.getZ() + eastWestMove, playerLoc.getYaw(),
                        playerLoc.getPitch()));
                    return;

                    // Otherwise, send him to spawn (first make sure he drops his flag/cake/bomb to prevent auto-cap and as punishment)
                } else {
                    playerWarzone.dropAllStolenObjects(warPlayer, false);
                    playerWarzone.respawnPlayer(event, event.getPlayer());
                    return;
                }
            }

            LoadoutSelection loadoutSelectionState = warPlayer.getLoadoutSelection();
            FlagReturn flagReturn = playerTeam.getTeamConfig().resolveFlagReturn();
            if (!playerTeam.isSpawnLocation(playerLoc)) {
                if (!playerWarzone.isEnoughPlayers() && loadoutSelectionState != null && loadoutSelectionState.isStillInSpawn()) {
                    // Be sure to keep only players that just respawned locked inside the spawn for minplayer/minteams restrictions - otherwise
                    // this will conflict with the can't-renter-spawn bump just a few lines below
                    War.war.badMsg(player, "zone.spawn.minplayers", playerWarzone.getWarzoneConfig().getInt(WarzoneConfig.MINPLAYERS), playerWarzone.getWarzoneConfig().getInt(WarzoneConfig.MINTEAMS));
                    event.setTo(playerTeam.getRandomSpawn());
                    return;
                }
                if (playerWarzone.isRespawning(warPlayer)) {
                    int rt = playerTeam.getTeamConfig().resolveInt(TeamConfig.RESPAWNTIMER);
                    War.war.badMsg(player, "zone.spawn.timer", rt);
                    event.setTo(playerTeam.getRandomSpawn());
                    return;
                }
                if (playerWarzone.isReinitializing()) {
                    // don't let players wander about outside spawns during reset
                    // (they could mess up the blocks that have already been reset
                    // before the start of the new battle)
                    War.war.msg(player, "zone.battle.reset");
                    event.setTo(playerTeam.getRandomSpawn());
                    return;
                }
            } else if (loadoutSelectionState != null && !loadoutSelectionState.isStillInSpawn() && !playerWarzone.isCakeThief(warPlayer) && (flagReturn.equals(FlagReturn.BOTH) || flagReturn
                .equals(FlagReturn.SPAWN)) && !playerWarzone.isFlagThief(warPlayer)) {

                // player is in spawn, but has left already: he should NOT be let back in - kick him out gently
                // if he sticks around too long.
                // (also, be sure you aren't preventing the flag or cake from being captured)
//				if (!CantReEnterSpawnJob.getPlayersUnderSuspicion().contains(player.getName())) {
//					CantReEnterSpawnJob job = new CantReEnterSpawnJob(player, playerTeam);
//					War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, job, 12);
//				}
                return;
            }

            // Monuments
            if (locZone != null && playerWarzone.nearAnyOwnedMonument(playerLoc, playerTeam) && player.getHealth() < 20 && player.getHealth() > 0 && this.random.nextInt(7) == 3) { // one chance out of many of getting healed
                int currentHp = (int) player.getHealth();
                int newHp = Math.min(20, currentHp + locZone.getWarzoneConfig().getInt(WarzoneConfig.MONUMENTHEAL));

                player.setHealth(newHp);
                double heartNum = ((double) newHp - currentHp) / 2;
                War.war.msg(player, "zone.monument.voodoo", heartNum);
                return;
            }

            // Flag capture
            if (playerWarzone.isFlagThief(warPlayer)) {

                // smoky
                if (System.currentTimeMillis() % 13 == 0) {
                    playerWarzone.getWorld().playEffect(player.getLocation(), Effect.POTION_BREAK, playerTeam.getKind().getPotionEffectColor());
                }

                // Make sure game ends can't occur simultaneously.
                // See Warzone.handleDeath() for details.
                boolean inSpawn = playerTeam.isSpawnLocation(player.getLocation());
                boolean inFlag = (playerTeam.getFlagVolume() != null && playerTeam.getFlagVolume().contains(player.getLocation()));

                if (playerTeam.getTeamConfig().resolveFlagReturn().equals(FlagReturn.BOTH)) {
                    if (!inSpawn && !inFlag) {
                        return;
                    }
                } else if (playerTeam.getTeamConfig().resolveFlagReturn().equals(FlagReturn.SPAWN)) {
                    if (inFlag) {
                        War.war.badMsg(player, "zone.flagreturn.spawn");
                        return;
                    } else if (!inSpawn) {
                        return;
                    }
                } else if (playerTeam.getTeamConfig().resolveFlagReturn().equals(FlagReturn.FLAG)) {
                    if (inSpawn) {
                        War.war.badMsg(player, "zone.flagreturn.flag");
                        return;
                    } else if (!inFlag) {
                        return;
                    }
                }

                if (!playerTeam.getPlayers().contains(warPlayer)) {
                    // Make sure player is still part of team, game may have ended while waiting)
                    // Ignore the scorers that happened immediately after the game end.
                    return;
                }

                if (playerWarzone.isTeamFlagStolen(playerTeam) && playerTeam.getTeamConfig().resolveBoolean(TeamConfig.FLAGMUSTBEHOME)) {
                    War.war.badMsg(player, "zone.flagreturn.deadlock");
                } else {
                    // flags can be captured at own spawn or own flag pole
                    if (playerWarzone.isReinitializing()) {
                        // Battle already ended or interrupted
                        playerWarzone.respawnPlayer(event, player);
                    } else {
                        // All good - proceed with scoring
                        playerTeam.addPoint();
                        Team victim = playerWarzone.getVictimTeamForFlagThief(warPlayer);

                        // Notify everyone
                        for (Team t : playerWarzone.getTeams()) {
                            t.teamcast("zone.flagcapture.broadcast", playerTeam.getKind().getColor() + player.getName() + ChatColor.WHITE, victim.getName(), playerTeam.getName());
                        }

                        // Detect win conditions
                        if (playerTeam.getPoints() >= playerTeam.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)) {
                            // if (playerWarzone.hasPlayerState(player.getName())) {
                            //     playerWarzone.restorePlayerState(player);
                            // }
                            playerWarzone.handleScoreCapReached(playerTeam.getName());
                        } else {
                            // just added a point
                            victim.getFlagVolume().resetBlocks(); // bring back flag to team that lost it
                            victim.initializeTeamFlag();

                            playerWarzone.respawnPlayer(event, player);
                            playerTeam.resetSign();
                        }
                    }

                    playerWarzone.removeFlagThief(warPlayer);

                    return;
                }
            }

            // Bomb detonation
            if (playerWarzone.isBombThief(warPlayer)) {
                // smoky
                playerWarzone.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 0);

                // Make sure game ends can't occur simultaneously.
                // Not thread safe. See Warzone.handleDeath() for details.
                boolean inEnemySpawn = false;
                Team victim = null;
                for (Team team : playerWarzone.getTeams()) {
                    if (team != playerTeam && team.isSpawnLocation(player.getLocation()) && team.getPlayers().size() > 0) {
                        inEnemySpawn = true;
                        victim = team;
                        break;
                    }
                }

                if (inEnemySpawn && playerTeam.hasPlayer(warPlayer)) {
                    // Made sure player is still part of team, game may have ended while waiting.
                    // Ignored the scorers that happened immediately after the game end.
                    Bomb bomb = playerWarzone.getBombForThief(warPlayer);

                    // Boom!
                    if (!playerWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.UNBREAKABLE)) {
                        // Don't blow up if warzone is unbreakable
                        playerWarzone.getWorld().createExplosion(player.getLocation(), 2F);
                    }

                    if (playerWarzone.isReinitializing()) {
                        // Battle already ended or interrupted
                        playerWarzone.respawnPlayer(event, player);
                    } else {
                        // All good - proceed with scoring
                        playerTeam.addPoint();

                        // Notify everyone
                        for (Team t : playerWarzone.getTeams()) {
                            t.teamcast("zone.bomb.broadcast", playerTeam.getKind().getColor() + player.getName() + ChatColor.WHITE, victim.getName(), playerTeam.getName());
                        }

                        // Detect win conditions
                        if (playerTeam.getPoints() >= playerTeam.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)) {
                            // if (playerWarzone.hasPlayerState(player.getName())) {
                            //     playerWarzone.restorePlayerState(player);
                            // }
                            playerWarzone.handleScoreCapReached(playerTeam.getName());
                        } else {
                            // just added a point

                            // restore bombed team's spawn
                            for (Volume spawnVolume : victim.getSpawnVolumes().values()) {
                                spawnVolume.resetBlocks();
                            }
                            victim.initializeTeamSpawns();

                            // bring back tnt
                            bomb.getVolume().resetBlocks();
                            bomb.addBombBlocks();

                            playerWarzone.respawnPlayer(event, player);
                            playerTeam.resetSign();
                        }
                    }

                    playerWarzone.removeBombThief(warPlayer);

                    return;
                }
            }

            // Cake retrieval
            if (playerWarzone.isCakeThief(warPlayer)) {
                // smoky
                if (System.currentTimeMillis() % 13 == 0) {
                    playerWarzone.getWorld().playEffect(player.getLocation(), Effect.POTION_BREAK, playerTeam.getKind().getPotionEffectColor());
                }

                // Make sure game ends can't occur simultaneously.
                // Not thread safe. See Warzone.handleDeath() for details.
                boolean inSpawn = playerTeam.isSpawnLocation(player.getLocation());

                if (inSpawn && playerTeam.getPlayers().contains(warPlayer)) {
                    // Made sure player is still part of team, game may have ended while waiting.
                    // Ignored the scorers that happened immediately after the game end.
                    boolean hasOpponent = false;
                    for (Team t : playerWarzone.getTeams()) {
                        if (t != playerTeam && t.getPlayers().size() > 0) {
                            hasOpponent = true;
                        }
                    }

                    // Don't let someone alone make points off cakes
                    if (hasOpponent) {
                        Cake cake = playerWarzone.getCakeForThief(warPlayer);

                        if (playerWarzone.isReinitializing()) {
                            // Battle already ended or interrupted
                            playerWarzone.respawnPlayer(event, player);
                        } else {
                            // All good - proceed with scoring
                            // Woot! Cake effect: 1 pt + full lifepool
                            playerTeam.addPoint();
                            playerTeam.setRemainingLives(playerTeam.getTeamConfig().resolveInt(TeamConfig.LIFEPOOL));

                            // Notify everyone
                            for (Team t : playerWarzone.getTeams()) {
                                t.teamcast("zone.cake.broadcast", playerTeam.getKind().getColor() + player.getName() + ChatColor.WHITE, ChatColor.GREEN + cake.getName() + ChatColor.WHITE,
                                    playerTeam.getName());
                            }

                            // Detect win conditions
                            if (playerTeam.getPoints() >= playerTeam.getTeamConfig().resolveInt(TeamConfig.MAXSCORE)) {
                                // if (playerWarzone.hasPlayerState(player.getName())) {
                                //     playerWarzone.restorePlayerState(player);
                                // }
                                playerWarzone.handleScoreCapReached(playerTeam.getName());
                            } else {
                                // just added a point

                                // bring back cake
                                cake.getVolume().resetBlocks();
                                cake.addCakeBlocks();

                                playerWarzone.respawnPlayer(event, player);
                                playerTeam.resetSign();
                            }
                        }

                        playerWarzone.removeCakeThief(warPlayer);
                    }

                    return;
                }
            }

            // Class selection lock
            if (!playerTeam.isSpawnLocation(player.getLocation()) && warPlayer.getLoadoutSelection().isStillInSpawn()) {
                warPlayer.getLoadoutSelection().setStillInSpawn(false);
            }

        } else if (locZone != null && !isMaker) {
            // player is not in any team, but inside warzone boundaries, get him out
            player.performCommand("spawn");
            War.war.badMsg(player, "zone.noteamnotice");
        } else {
            // player is not in a warzone
            ZonePortal portal = War.war.getZonePortal(playerLoc);
            if (portal != null) {
                Warzone zone = portal.getZone();
                if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED) || zone.isReinitializing()) {
                    War.war.badMsg(player, "join.disabled");
                } else if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.JOINMIDBATTLE) && zone.isEnoughPlayers()) {
                    War.war.badMsg(player, "join.progress");
                } else if (zone.isFull()) {
                    War.war.badMsg(player, "join.full.all");
                } else if (zone.isFull(player)) {
                    War.war.badMsg(player, "join.permission.all");
                } else {
                    zone.autoAssign(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (War.war.isLoaded() && event.isSneaking()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Warzone zone = warPlayer.getZone();
            Team team = warPlayer.getTeam();
            if (zone == null || team == null) {
                return;
            }
            if (team.getInventories().resolveLoadouts().isEmpty()) {
                return;
            }

            LoadoutSelection selection = warPlayer.getLoadoutSelection();
            if (team.isSpawnLocation(player.getLocation())) {
                if (!selection.isStillInSpawn()) {
                    War.war.badMsg(player, "zone.class.reenter");
                } else {
                    player.performCommand(War.war.getWarConfig().getString(WarConfig.LOADOUTCMD));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone playingZone = warPlayer.getZone();
        Warzone deadZone = Warzone.getZoneForDeadPlayer(player);
        if (playingZone == null && deadZone != null) {
            // Game ended while player was dead, so restore state
            deadZone.getReallyDeadFighters().remove(player.getUniqueId());
            warPlayer.restorePlayerState();
            return;
        } else if (playingZone == null) {
            // Player not playing war
            return;
        } else if (deadZone == null) {
            // Player is not a 'really' dead player, nothing to do here
            return;
        }
        Team team = warPlayer.getTeam();

        Validate.notNull(team, String.format("Failed to find a team for player %s in warzone %s on respawn.", event.getPlayer().getName(), playingZone.getName()));
        playingZone.getReallyDeadFighters().remove(player.getUniqueId());
        event.setRespawnLocation(team.getRandomSpawn());
        playingZone.respawnPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
            return;
        }
        if (War.war.isLoaded()) {
            Player player = event.getPlayer();
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Warzone zone = warPlayer.getZone();
            if (zone != null) {
                if (!zone.getVolume().contains(event.getTo())) {
                    // Prevent teleporting out of the warzone
                    if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.REALDEATHS)) {
                        War.war.badMsg(event.getPlayer(), "Use /leave (or /war leave) to exit the zone.");
                    }
                    zone.dropAllStolenObjects(warPlayer, false);
                    zone.respawnPlayer(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (War.war.isLoaded()) {
            WarPlayer warPlayer = WarPlayer.getPlayer(event.getPlayer().getUniqueId());
            Team team = warPlayer.getTeam();
            if (team != null && team.getTeamConfig().resolveBoolean(TeamConfig.XPKILLMETER)) {
                event.setAmount(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        WarPlayer warPlayer = WarPlayer.getPlayer(event.getPlayer().getUniqueId());
        Team team = warPlayer.getTeam();
        if (team != null && team.isInTeamChat(warPlayer)) {
            event.setCancelled(true);
            team.sendTeamChatMessage(event.getPlayer(), event.getMessage());
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();
        if (zone == null) {
            return;
        }
        if (zone.isThief(warPlayer)) {
            // Prevent thieves from taking their bomb/wool/cake into a chest, etc.
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 10, 10);
        } else if (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getSlot() == 39 && zone.getWarzoneConfig().getBoolean(WarzoneConfig.BLOCKHEADS)) {
            // Magically give player a wool block when they click their helmet
            ItemStack teamBlock = warPlayer.getTeam().getKind().getBlockHead();
            player.getInventory().remove(teamBlock.getType());
            // Deprecated behavior cannot be removed as it is essential to this function
            //noinspection deprecation
            event.setCursor(teamBlock);
            event.setCancelled(true);
        }
    }

    public void purgeLatestPositions() {
        this.latestLocations.clear();
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = (Player) event.getPlayer();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();
        if (zone == null) {
            return;
        }
        if (zone.isImportantBlock(event.getBlockClicked())) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 10, 10);
        }
    }

    @EventHandler
    public void onPlayerDamageItem(PlayerItemDamageEvent event) {
        Player player = (Player) event.getPlayer();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();
        if (zone == null) {
            return;
        }
        event.setCancelled(true);
    }
}
