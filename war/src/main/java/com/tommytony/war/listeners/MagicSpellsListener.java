package com.tommytony.war.listeners;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.MinionTargetEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.targeted.HealSpell;
import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.utility.LoadoutSelection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

public class MagicSpellsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMinionTarget(MinionTargetEvent event) {
        if (!War.war.isLoaded()) {
            return;
        }

        Player owner = event.getOwner();
        LivingEntity target = event.getTarget();
        if (target instanceof Player) {
            WarPlayer warOwner = WarPlayer.getPlayer(owner.getUniqueId());
            WarPlayer warTarget = WarPlayer.getPlayer(target.getUniqueId());

            Team ownerTeam = warOwner.getTeam();
            Team targetTeam = warTarget.getTeam();
            if (ownerTeam == null || targetTeam == null) {
                event.setCancelled(true);
                return;
            }

            Warzone ownerZone = warOwner.getZone();
            Warzone targetZone = warTarget.getZone();
            if (!(ownerZone.getName().equals(targetZone.getName()))) {
                // Different warzones
                event.setCancelled(true);
                return;
            }

            if (ownerTeam.getName().equals(targetTeam.getName())) {
                // Same team
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpellCast(SpellCastEvent event) {
        Player caster = event.getCaster();

        WarPlayer casterWarPlayer = WarPlayer.getPlayer(caster.getUniqueId());
        Team team = casterWarPlayer.getTeam();
        if (team == null) {
            return;
        }

        LoadoutSelection casterLoadoutState = casterWarPlayer.getLoadoutSelection();
        if (team.isSpawnLocation(caster.getLocation()) && casterLoadoutState.isStillInSpawn()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpellTarget(SpellTargetEvent event) {
        Player caster = event.getCaster();
        LivingEntity livingEntity = event.getTarget();

        if (livingEntity instanceof Player) {
            Spell spell = event.getSpell();
            Player target = (Player) livingEntity;

            WarPlayer targetWarPlayer = WarPlayer.getPlayer(target.getUniqueId());
            WarPlayer casterWarPlayer = WarPlayer.getPlayer(caster.getUniqueId());

            Warzone targetZone = targetWarPlayer.getZone();
            Warzone casterZone = casterWarPlayer.getZone();
            if (targetZone == null && casterZone == null) {
                // Neither player is in a warzone, so only allow beneficial spells to target
                if (!spell.isBeneficial()) {
                    event.setCancelled(true);
                }
                return;
            }

            if (targetZone == null) {
                War.war.badMsg(caster, "pvp.target.notplaying");
                event.setCancelled(true);
                return;
            } else if (casterZone == null) {
                War.war.badMsg(caster, "pvp.self.notplaying");
                event.setCancelled(true);
                return;
            }

            if (!targetZone.getName().equals(casterZone.getName())) {
                // Only players in the same zone can damage each other
                War.war.badMsg(caster, "pvp.target.otherzone");
                event.setCancelled(true);
                return;
            }

            Team targetTeam = targetWarPlayer.getTeam();
            Team casterTeam = casterWarPlayer.getTeam();
            boolean friendlyFire = casterZone.getWarzoneConfig().getBoolean(WarzoneConfig.FRIENDLYFIRE);
            if (casterTeam.getName().equals(targetTeam.getName())) {
                if (!friendlyFire && !spell.isBeneficial()) {
                    // Team kill is disabled, and the spell is harmful
                    event.setCancelled(true);
                    return;
                }
            } else {
                // Target is not on our team, and our spell is beneficial
                if (spell.isBeneficial()) {
                    event.setCancelled(true);
                    return;
                }
            }

            LoadoutSelection targetLoadoutState = targetWarPlayer.getLoadoutSelection();
            if (targetTeam.isSpawnLocation(target.getLocation()) && targetLoadoutState.isStillInSpawn()) {
                // Target is in spawn
                event.setCancelled(true);
                return;
            }

            LoadoutSelection casterLoadoutState = casterWarPlayer.getLoadoutSelection();
            if (casterTeam.isSpawnLocation(caster.getLocation()) && casterLoadoutState.isStillInSpawn()) {
                // Caster is in spawn
                event.setCancelled(true);
                return;
            }

            if (spell instanceof HealSpell) {
                HealSpell healSpell = (HealSpell) spell;

                try {
                    Field field = healSpell.getClass().getDeclaredField("healAmount");
                    field.setAccessible(true);
                    Double healAmount = (Double) field.get(healSpell);
                    healAmount = Math.min(healAmount, target.getMaxHealth() - Math.round(target.getHealth()));

                    casterWarPlayer.addHeal(target, healAmount / 2.0);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
