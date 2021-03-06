package com.tommytony.war.listeners;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.buff.MinionSpell;
import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.WarConfig;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.event.WarPlayerDeathEvent;
import com.tommytony.war.event.WarPlayerKillEvent;
import com.tommytony.war.runnable.DeferredBlockResetsJob;
import com.tommytony.war.structure.Bomb;
import com.tommytony.war.utility.LoadoutSelection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles Entity-Events
 *
 * @author tommytony, Tim Düsterhus
 * @package com.tommytony.war.event
 */
public class WarEntityListener implements Listener {

    private static final HashSet<PotionEffectType> HARMFUL_POTIONS = new HashSet<>();
    private static final HashSet<PotionEffectType> BENEFICIAL_POTIONS = new HashSet<>();

    static {
        HARMFUL_POTIONS.add(PotionEffectType.HARM);
        HARMFUL_POTIONS.add(PotionEffectType.POISON);
        HARMFUL_POTIONS.add(PotionEffectType.WEAKNESS);
        HARMFUL_POTIONS.add(PotionEffectType.SLOW);

        BENEFICIAL_POTIONS.add(PotionEffectType.SPEED);
        BENEFICIAL_POTIONS.add(PotionEffectType.INCREASE_DAMAGE);
        BENEFICIAL_POTIONS.add(PotionEffectType.REGENERATION);
        BENEFICIAL_POTIONS.add(PotionEffectType.DAMAGE_RESISTANCE);
        BENEFICIAL_POTIONS.add(PotionEffectType.FIRE_RESISTANCE);
        BENEFICIAL_POTIONS.add(PotionEffectType.FAST_DIGGING);
        BENEFICIAL_POTIONS.add(PotionEffectType.ABSORPTION);
        BENEFICIAL_POTIONS.add(PotionEffectType.HEAL);
        BENEFICIAL_POTIONS.add(PotionEffectType.HEALTH_BOOST);
        BENEFICIAL_POTIONS.add(PotionEffectType.INVISIBILITY);
        BENEFICIAL_POTIONS.add(PotionEffectType.NIGHT_VISION);
        BENEFICIAL_POTIONS.add(PotionEffectType.WATER_BREATHING);
        BENEFICIAL_POTIONS.add(PotionEffectType.JUMP);
    }


    /**
     * Handles PVP-Damage
     *
     * @param event fired event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();

        // Maybe an arrow was thrown
        if (attacker instanceof Projectile && ((Projectile) attacker).getShooter() instanceof Player) {
            attacker = ((Player) ((Projectile) attacker).getShooter());
        }

        if (attacker == null || defender == null) {
            return;
        }

        if (!(defender instanceof Player)) {
            return;
        }

        Player d = (Player) defender;
        WarPlayer warDefender = WarPlayer.getPlayer(defender.getUniqueId());
        if (warDefender.getZone() == null) {
            return;
        }

        LoadoutSelection defenderLoadoutState = warDefender.getLoadoutSelection();
        if (defenderLoadoutState.isStillInSpawn()) {
            event.setCancelled(true);
            return;
        }

        if (attacker instanceof Player) {
            // only let adversaries (same warzone, different team) attack each other
            Player a = (Player) attacker;
            WarPlayer warAttacker = WarPlayer.getPlayer(a.getUniqueId());
            Warzone attackerWarzone = warAttacker.getZone();
            Team attackerTeam = warAttacker.getTeam();
            Warzone defenderWarzone = warDefender.getZone();
            Team defenderTeam = warDefender.getTeam();

            if ((attackerTeam != null && defenderTeam != null && attackerTeam != defenderTeam && attackerWarzone == defenderWarzone) || (attackerTeam != null && defenderTeam != null
                && attacker.getEntityId() == defender.getEntityId())) {

                LoadoutSelection attackerLoadoutState = warAttacker.getLoadoutSelection();
                if (attackerLoadoutState.isStillInSpawn()) {
                    // War.war.badMsg(a, "pvp.self.spawn");
                    event.setCancelled(true);
                    return;
                }

                // Make sure none of them are locked in by respawn timer
                if (defenderWarzone.isRespawning(warDefender)) {
                    War.war.badMsg(a, "pvp.target.respawn");
                    event.setCancelled(true);
                    return;
                } else if (attackerWarzone.isRespawning(warAttacker)) {
                    War.war.badMsg(a, "pvp.self.respawn");
                    event.setCancelled(true);
                    return;
                }

                if (!defenderWarzone.getPvpReady()) {
                    event.setCancelled(true);
                    return;
                }

                if (!attackerWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.PVPINZONE)) {
                    // spleef-like, non-pvp, zone
                    event.setCancelled(true);
                    return;
                }

                // Detect death, prevent it and respawn the player
                if (event.getFinalDamage() >= d.getHealth()) {
                    if (defenderWarzone.getReallyDeadFighters().contains(d.getUniqueId())) {
                        // don't re-kill a dead person
                        return;
                    }
                    WarPlayerDeathEvent deathEvent = new WarPlayerDeathEvent(defenderWarzone, d, a, event.getCause());
                    War.war.getServer().getPluginManager().callEvent(deathEvent);

                    WarPlayerKillEvent killEvent = new WarPlayerKillEvent(attackerWarzone, a, d, event.getCause());
                    War.war.getServer().getPluginManager().callEvent(killEvent);

                    if (!defenderWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.REALDEATHS)) {
                        // fast respawn, don't really die
                        event.setCancelled(true);
                    }
                    if (d == a) {
                        defenderWarzone.handleSuicide(d);
                    } else {
                        defenderWarzone.handleKill(a, d, event.getDamager(), true);
                    }
                } else if (defenderWarzone.isBombThief(warDefender) && d.getLocation().distance(a.getLocation()) < 2) {
                    // Close combat, close enough to detonate
                    Bomb bomb = defenderWarzone.getBombForThief(warDefender);

                    // Kill the bomber
                    WarPlayerDeathEvent event1 = new WarPlayerDeathEvent(defenderWarzone, d, null, event.getCause());
                    War.war.getServer().getPluginManager().callEvent(event1);
                    defenderWarzone.handleDeath(d);

                    if (defenderWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.REALDEATHS)) {
                        // and respawn him and remove from deadmen (cause realdeath + handleDeath means no respawn and getting queued up for onPlayerRespawn)
                        defenderWarzone.getReallyDeadFighters().remove(d.getUniqueId());
                        defenderWarzone.respawnPlayer(d);
                    }

                    // Blow up bomb
                    if (!defenderWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.UNBREAKABLE)) {
                        defenderWarzone.getWorld().createExplosion(a.getLocation(), 2F);
                    }

                    // bring back tnt
                    bomb.getVolume().resetBlocks();
                    bomb.addBombBlocks();

                    // Notify everyone
                    for (Team t : defenderWarzone.getTeams()) {
                        t.teamcast("pvp.kill.bomb", attackerTeam.getKind().getColor() + a.getName() + ChatColor.WHITE, defenderTeam.getKind().getColor() + d.getName() + ChatColor.WHITE);
                    }
                } else {
                    // Regular damage (but not enough to kill), so record it
                    warDefender.setLastDamager(a, event.getDamager());
                }
            } else if (attackerTeam != null && defenderTeam != null && attackerTeam == defenderTeam && attackerWarzone == defenderWarzone && attacker.getEntityId() != defender.getEntityId()) {
                // same team, but not same person
                if (attackerWarzone.getWarzoneConfig().getBoolean(WarzoneConfig.FRIENDLYFIRE)) {
                    War.war.badMsg(a, "pvp.ff.enabled"); // if ff is on, let the attack go through
                    warDefender.setLastDamager(warAttacker.getPlayer(), warAttacker.getPlayer());
                } else {
                    War.war.badMsg(a, "pvp.ff.disabled");
                    event.setCancelled(true); // ff is off
                }
            } else if (attackerTeam == null && defenderTeam == null && War.war.canPvpOutsideZones(a)) {
                // let normal PVP through is its not turned off or if you have perms
                warDefender.setLastDamager(warAttacker.getPlayer(), warAttacker.getPlayer());
            } else if (attackerTeam == null && defenderTeam == null && !War.war.canPvpOutsideZones(a)) {
                if (!War.war.getWarConfig().getBoolean(WarConfig.DISABLEPVPMESSAGE)) {
                    War.war.badMsg(a, "pvp.outside.permission");
                }

                event.setCancelled(true); // global pvp is off
            } else {
                if (attackerTeam == null) {
                    War.war.badMsg(a, "pvp.self.notplaying");
                } else if (defenderTeam == null) {
                    War.war.badMsg(a, "pvp.target.notplaying");
                } else if (attacker.getEntityId() == defender.getEntityId()) {
                    // You just hit yourself, probably with a bouncing arrow
                } else if (attackerTeam == defenderTeam) {
                    War.war.badMsg(a, "pvp.ff.disabled");
                }

                event.setCancelled(true); // can't attack someone inside a warzone if you're not in a team
            }
        } else if (attacker instanceof LivingEntity) {
             LivingEntity monster = (LivingEntity) attacker;
             Warzone zone = warDefender.getZone();
             if (zone == null) {
                 return;
             }

             boolean isDeath = event.getFinalDamage() >= d.getHealth();
             for (Spell spell : MagicSpells.spells()) {
                 if (!(spell instanceof MinionSpell)) {
                     continue;
                 }
                 MinionSpell minionSpell = (MinionSpell) spell;
                 try {
                     Field field = minionSpell.getClass().getDeclaredField("players");
                     field.setAccessible(true);
                     Map<LivingEntity, UUID> players = (Map<LivingEntity, UUID>) field.get(minionSpell);
                     if (!players.containsKey(monster)) {
                         continue;
                     }

                     Player caster = Bukkit.getPlayer(players.get(monster));
                     if (caster == null) {
                         continue;
                     }

                     if (isDeath) {
                         WarPlayerDeathEvent deathEvent = new WarPlayerDeathEvent(warDefender.getZone(), d, caster, event.getCause());
                         War.war.getServer().getPluginManager().callEvent(deathEvent);

                         zone.handleKill(caster, warDefender.getPlayer(), monster, true);
                     } else {
                         warDefender.setLastDamager(caster, monster);
                     }
                     return;
                 } catch (Exception ignored) {
                     return;
                 }
             }
             if (isDeath) {
                 WarPlayerDeathEvent deathEvent = new WarPlayerDeathEvent(warDefender.getZone(), d, attacker, event.getCause());
                 War.war.getServer().getPluginManager().callEvent(deathEvent);

                 zone.handleNaturalKill(warDefender.getPlayer(), event);
             }
        }
    }

    /**
     * Protects important structures from explosions
     */
    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }
        // protect zones elements, lobbies and warhub from creepers and tnt
        List<Block> explodedBlocks = event.blockList();
        List<Block> dontExplode = new ArrayList<>();

        boolean explosionInAWarzone = event.getEntity() != null && Warzone.getZoneByLocation(event.getEntity().getLocation()) != null;

        if (!explosionInAWarzone && War.war.getWarConfig().getBoolean(WarConfig.TNTINZONESONLY) && event.getEntity() instanceof TNTPrimed) {
            // if tntinzonesonly:true, no tnt blows up outside zones
            event.setCancelled(true);
            return;
        }

        for (Block block : explodedBlocks) {
            if (block.getType() == Material.TNT) {
                continue; // don't restore TNT (failed to track down regression cause)
            }
            boolean inOneZone = false;
            for (Warzone zone : War.war.getWarzones()) {
                if (zone.isImportantBlock(block)) {
                    dontExplode.add(block);
                    if (zone.isBombBlock(block)) {
                        // tnt doesn't get reset like normal blocks, gotta schedule a later reset just for the Bomb
                        // structure's tnt block
                        DeferredBlockResetsJob job = new DeferredBlockResetsJob();
                        BlockState tnt = block.getState();
                        tnt.setType(Material.TNT);
                        job.add(tnt);
                        War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, job, 10);
                    }
                    inOneZone = true;
                    break;
                } else if (zone.getVolume().contains(block)) {
                    inOneZone = true;
                    break;
                }
            }

            if (!inOneZone && explosionInAWarzone) {
                // if the explosion originated in warzone, always rollback
                dontExplode.add(block);
            }
        }

        int dontExplodeSize = dontExplode.size();
        if (dontExplode.isEmpty()) {
            return;
        }
        // Reset the exploded blocks that shouldn't have exploded (some of these are zone artifacts, if rollbackexplosion some may be outside-of-zone blocks
        DeferredBlockResetsJob job = new DeferredBlockResetsJob();
        for (Block dont : dontExplode) {
            job.add(dont.getState());
        }
        War.war.getServer().getScheduler().scheduleSyncDelayedTask(War.war, job);

        // Changed explosion yield following proportion of explosion prevention (makes drops less buggy too)
        int explodedSize = explodedBlocks.size();
        float middleYeild = (float) (explodedSize - dontExplodeSize) / (float) explodedSize;
        float newYeild = middleYeild * event.getYield();

        event.setYield(newYeild);
    }

    /**
     * Handles damage on Players
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            // Let the actual EntityDamageByEntityEvent handler take care of it
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();
        Team team = warPlayer.getTeam();
        if (team == null || zone == null) {
            return;
        }

        LoadoutSelection playerLoadoutState = warPlayer.getLoadoutSelection();
        if (team.isSpawnLocation(player.getLocation()) && playerLoadoutState != null && playerLoadoutState.isStillInSpawn()) {
            // don't let a player still in spawn get damaged
            event.setCancelled(true);
            return;
        }

        if (event.getFinalDamage() >= player.getHealth()) {
            if (zone.getReallyDeadFighters().contains(player.getUniqueId())) {
                // don't re-count the death points of an already dead person
                return;
            }

            // Detect death, prevent it and respawn the player
            WarPlayerDeathEvent deathEvent = new WarPlayerDeathEvent(zone, player, null, event.getCause());
            War.war.getServer().getPluginManager().callEvent(deathEvent);

            if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.REALDEATHS)) {
                // fast respawn, don't really die
                event.setCancelled(true);
            }
            zone.handleNaturalKill(player, event);
        }
    }

    /**
     * Prevents creatures from spawning in warzones if no creatures is active
     */
    @EventHandler
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        Location location = event.getLocation();
        Warzone zone = Warzone.getZoneByLocation(location);
        if (zone != null && zone.getWarzoneConfig().getBoolean(WarzoneConfig.NOCREATURES)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents health regaining caused by peaceful mode
     */
    @EventHandler
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();
        if (zone == null) {
            return;
        }

        Team team = warPlayer.getTeam();
        if (event.getRegainReason() == RegainReason.SATIATED && team.getTeamConfig().resolveBoolean(TeamConfig.NOHUNGER)) {
            // noHunger setting means you can't auto-heal with full hunger bar (use saturation instead to control how fast you get hungry)
            event.setCancelled(true);
        } else if (event.getRegainReason() == RegainReason.REGEN) {
            // disable peaceful mode regen
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        if (!War.war.isLoaded() || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();
        Team team = warPlayer.getTeam();
        if (zone != null && team.getTeamConfig().resolveBoolean(TeamConfig.NOHUNGER)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Player player = event.getEntity();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Warzone zone = warPlayer.getZone();

        if (zone == null) {
            return;
        }
        event.getDrops().clear();
        if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.REALDEATHS)) {
            // catch the odd death that gets away from us when usually intercepting and preventing deaths
            zone.handleDeath(player);
            Team team = warPlayer.getTeam();
            if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.DEATHMESSAGES)) {
                zone.broadcast("pvp.death.other", team.getKind().getColor() + player.getName());
            }
            War.war.getLogger().log(Level.WARNING, "We missed the death of player {0} - something went wrong.", player.getName());
        } else {
            event.setDeathMessage("");
        }
    }

    @EventHandler
    public void onExplosionPrime(final ExplosionPrimeEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        Location eventLocation = event.getEntity().getLocation();

        for (Warzone zone : War.war.getWarzones()) {
            if (zone.isBombBlock(eventLocation.getBlock())) {
                // prevent the Bomb from exploding on its pedestral
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }
        if (event.getEntityType() == EntityType.EGG) {
            if (!event.getEntity().hasMetadata("warAirstrike")) {
                return;
            }
            Location loc = event.getEntity().getLocation();
            Warzone zone = Warzone.getZoneByLocation(loc);
            if (zone == null) {
                return;
            }
            Location tntPlace = new Location(loc.getWorld(), loc.getX(), Warzone.getZoneByLocation(loc).getVolume().getMaxY(), loc.getZ());
            loc.getWorld().spawnEntity(tntPlace, EntityType.PRIMED_TNT);
            loc.getWorld().spawnEntity(tntPlace.clone().add(new Vector(2, 0, 0)), EntityType.PRIMED_TNT);
            loc.getWorld().spawnEntity(tntPlace.clone().add(new Vector(-2, 0, 0)), EntityType.PRIMED_TNT);
            loc.getWorld().spawnEntity(tntPlace.clone().add(new Vector(0, 0, 2)), EntityType.PRIMED_TNT);
            loc.getWorld().spawnEntity(tntPlace.clone().add(new Vector(0, 0, -2)), EntityType.PRIMED_TNT);
        }
    }

    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }
        if (event.getEntityType() == EntityType.EGG) {
            ProjectileSource shooter = event.getEntity().getShooter();
            if (!(shooter instanceof Player)) {
                return;
            }

            Player player = (Player) shooter;
            WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
            Warzone zone = warPlayer.getZone();
            if (zone == null) {
                return;
            }

            Team team = warPlayer.getTeam();
            if (War.war.getKillstreakReward().getAirstrikePlayers().remove(player.getName())) {
                event.getEntity().setMetadata("warAirstrike", new FixedMetadataValue(War.war, true));
                zone.broadcast("zone.airstrike", team.getKind().getColor() + player.getName() + ChatColor.WHITE);
            }
        }
    }

    @EventHandler
    public void onPaintingBreakByEntity(final HangingBreakByEntityEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }
        if (!(event.getRemover() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getRemover();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team team = warPlayer.getTeam();
        boolean isZoneMaker = War.war.isZoneMaker(player);
        if (team == null && isZoneMaker) {
            return;
        }

        Warzone zone = Warzone.getZoneByLocation(event.getEntity().getLocation());
        if (zone != null && zone.getWarzoneConfig().getBoolean(WarzoneConfig.UNBREAKABLE)) {
            event.setCancelled(true);
            War.war.badMsg(player, "build.denied.zone.break");
        }
    }

    @EventHandler
    public void onPaintingPlaceByEntity(final HangingPlaceEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        Player player = event.getPlayer();
        WarPlayer warPlayer = WarPlayer.getPlayer(player.getUniqueId());
        Team team = warPlayer.getTeam();
        boolean isZoneMaker = War.war.isZoneMaker(player);
        if (team == null && isZoneMaker) {
            return;
        }

        Warzone zone = Warzone.getZoneByLocation(event.getBlock().getLocation());
        if (zone != null && (zone.getWarzoneConfig().getBoolean(WarzoneConfig.UNBREAKABLE) || (team != null && !team.getTeamConfig().resolveBoolean(TeamConfig.PLACEBLOCK)))) {
            event.setCancelled(true);
            War.war.badMsg(player, "build.denied.zone.place");
        }
    }

    @EventHandler
    public void onEntityTeleport(final EntityTeleportEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }
        if (event.getEntityType() == EntityType.WOLF) {
            if (Warzone.getZoneByLocation(event.getTo()) != null) {
                // prevent wolves from teleporting to players in zones
                event.setCancelled(true);
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ProjectileSource shooter = potion.getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }

        Player thrower = (Player) shooter;
        WarPlayer warThrower = WarPlayer.getPlayer(thrower.getUniqueId());
        if (warThrower.isSpectating()) {
            event.setCancelled(true);
            return;
        }

        Warzone zone = warThrower.getZone();
        if (zone == null) {
            return;
        }

        boolean beneficial = true;
        Collection<PotionEffect> effects = potion.getEffects();
        for (PotionEffect effect : effects) {
            if (HARMFUL_POTIONS.contains(effect.getType())) {
                beneficial = false;
                break;
            } else if (BENEFICIAL_POTIONS.contains(effect.getType())) {
                break;
            }
        }

        Collection<LivingEntity> affected = event.getAffectedEntities();
        for (LivingEntity target : affected) {
            if (!(target instanceof Player)) {
                continue;
            }

            Player player = (Player) target;
            WarPlayer warTarget = WarPlayer.getPlayer(player.getUniqueId());
            if (warTarget.getZone() == null) {
                event.setIntensity(target, 0);
            } else {
                boolean sameTeam = warTarget.getTeam() == warThrower.getTeam();
                boolean shouldAffect = beneficial == sameTeam;
                if (!shouldAffect) {
                    event.setIntensity(target, 0);
                }
            }
        }
    }
}
