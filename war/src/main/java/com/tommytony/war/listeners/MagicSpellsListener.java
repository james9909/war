package com.tommytony.war.listeners;

import org.bukkit.event.Listener;

public class MagicSpellsListener implements Listener {

    /*
    @EventHandler(priority=EventPriority.NORMAL)
    public void onSpellTarget(SpellTargetEvent event) {
        Player caster = event.getCaster();
        LivingEntity livingEntity = event.getTarget();

        if (livingEntity instanceof Player) {

            Player target = (Player) livingEntity;
            ArenaPlayer aTarget = ArenaPlayer.parsePlayer(target.getName());
            ArenaPlayer aCaster = ArenaPlayer.parsePlayer(caster.getName());
            if (aTarget == null || aCaster == null) {
                // Sanity check
                event.setCancelled(true);
                return;
            }

            Arena targetArena = aTarget.getArena();
            Arena casterArena = aCaster.getArena();
            if (targetArena == null || casterArena == null) {
                // Only players who are in arenas can damage other arena players
                event.setCancelled(true);
                return;
            }
            if (!casterArena.getName().equals(targetArena.getName())) {
                // Players must be in the same arena
                event.setCancelled(true);
                return;
            }

            ArenaTeam casterTeam = aCaster.getArenaTeam();
            ArenaTeam targetTeam = aTarget.getArenaTeam();
            if (casterTeam == null || targetTeam == null) {
                // No team
                event.setCancelled(true);
                return;
            }

            Spell spell = event.getSpell();
            boolean teamKill = casterArena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL);
            if (casterTeam.getName().equals(targetTeam.getName())) {
                if (!teamKill && !spell.isBeneficial()) {
                    // Team kill is disabled, and the spell is harmful
                    event.setCancelled(true);
                    return;
                }
            }

            int spawnProtection = targetArena.getArenaConfig().getInt(CFG.PROTECT_SPAWN);
            if (spawnProtection > 0 && SpawnManager.isNearSpawn(targetArena, target, spawnProtection)) {
                // Target is in spawn
                event.setCancelled(true);
                return;
            }

            if (spawnProtection > 0 && SpawnManager.isNearSpawn(casterArena, caster, spawnProtection)) {
                // Caster is in spawn
                event.setCancelled(true);
                return;
            }
        }
    }
    */
}
