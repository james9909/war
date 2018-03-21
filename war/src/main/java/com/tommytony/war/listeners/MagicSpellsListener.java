package com.tommytony.war.listeners;

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.targeted.HealSpell;
import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.utility.LoadoutSelection;
import java.lang.reflect.Field;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MagicSpellsListener implements Listener {

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellTarget(SpellTargetEvent event) {
        Player caster = event.getCaster();
        LivingEntity livingEntity = event.getTarget();

        if (livingEntity instanceof Player) {

            Spell spell = event.getSpell();
            Player target = (Player) livingEntity;

            WarPlayer targetWarPlayer = WarPlayer.getPlayer(target.getUniqueId());
            WarPlayer casterWarPlayer = WarPlayer.getPlayer(caster.getUniqueId());

            Team targetTeam = targetWarPlayer.getTeam();
            Team casterTeam = casterWarPlayer.getTeam();
            if (targetTeam == null && casterTeam == null) {
                // Neither player is in a warzone, so only allow beneficial spells to target
                if (!spell.isBeneficial()) {
                    System.out.printf("Prevented %s from being cast (outside + harmful)\n", spell.getName());
                    event.setCancelled(true);
                }
                return;
            }

            if (targetTeam == null) {
                War.war.badMsg(caster, "pvp.target.notplaying");
                event.setCancelled(true);
                return;
            } else if (casterTeam == null) {
                War.war.badMsg(caster, "pvp.self.notplaying");
                event.setCancelled(true);
                return;
            }

            Warzone targetZone = targetTeam.getZone();
            Warzone casterZone = casterTeam.getZone();
            if (targetZone == null) {
                event.setCancelled(true);
                return;
            } else if (casterZone == null) {
                event.setCancelled(true);
                return;
            }

            if (!targetZone.getName().equals(casterZone.getName())) {
                // Only players in the same zone can damage each other
                War.war.badMsg(caster, "pvp.target.otherzone");
                event.setCancelled(true);
                return;
            }

            boolean friendlyFire = casterZone.getWarzoneConfig().getBoolean(WarzoneConfig.FRIENDLYFIRE);
            if (casterTeam.getName().equals(targetTeam.getName())) {
                if (!friendlyFire && !spell.isBeneficial()) {
                    // Team kill is disabled, and the spell is harmful
                    System.out.printf("Prevented %s from being cast (team + harmful)\n", spell.getName());
                    event.setCancelled(true);
                    return;
                }
            } else {
                // Target is not on our team, and our spell is beneficial
                if (spell.isBeneficial()) {
                    System.out.printf("Prevented %s from being cast (enemy + beneficial)\n", spell.getName());
                    event.setCancelled(true);
                    return;
                }
            }

            LoadoutSelection targetLoadoutState = targetWarPlayer.getLoadoutSelection();
            if (targetTeam.isSpawnLocation(target.getLocation()) && targetLoadoutState.isStillInSpawn()) {
                // Target is in spawn
                War.war.badMsg(caster, "pvp.target.spawn");
                event.setCancelled(true);
                return;
            }

            LoadoutSelection casterLoadoutState = casterWarPlayer.getLoadoutSelection();
            if (casterTeam.isSpawnLocation(caster.getLocation()) && casterLoadoutState.isStillInSpawn()) {
                // Caster is in spawn
                War.war.badMsg(caster, "pvp.self.spawn");
                event.setCancelled(true);
                return;
            }

            if (spell instanceof HealSpell) {
                HealSpell healSpell = (HealSpell) spell;

                try {
                    Field field = healSpell.getClass().getDeclaredField("healAmount");
                    field.setAccessible(true);
                    Double healAmount = (Double) field.get(healSpell);
                    healAmount = Math.min(healAmount, target.getAttribute(GENERIC_MAX_HEALTH).getValue() - target.getHealth());

                    System.out.printf("Healed %s for %f hearts\n", target.getName(), healAmount);
                    casterWarPlayer.addHeal(target, healAmount);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
