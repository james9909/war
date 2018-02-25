package com.tommytony.war.listeners;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.utility.LoadoutSelection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MagicSpellsListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellTarget(SpellTargetEvent event) {
        Player caster = event.getCaster();
        LivingEntity livingEntity = event.getTarget();

        if (livingEntity instanceof Player) {

            Player target = (Player) livingEntity;

            Team targetTeam = Team.getTeamByPlayerName(target.getName());
            Team casterTeam = Team.getTeamByPlayerName(caster.getName());
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

            Spell spell = event.getSpell();
            boolean teamKill = casterZone.getWarzoneConfig().getBoolean(WarzoneConfig.FRIENDLYFIRE);
            if (casterTeam.getName().equals(targetTeam.getName())) {
                if (!teamKill && !spell.isBeneficial()) {
                    // Team kill is disabled, and the spell is harmful
                    event.setCancelled(true);
                    return;
                }
            }

            LoadoutSelection targetLoadoutState = targetZone.getLoadoutSelections().get(target.getName());
            if (targetTeam.isSpawnLocation(target.getLocation()) && targetLoadoutState.isStillInSpawn()) {
                // Target is in spawn
                War.war.badMsg(caster, "pvp.target.spawn");
                event.setCancelled(true);
                return;
            }
        }
    }
}
