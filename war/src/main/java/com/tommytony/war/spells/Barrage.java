package com.tommytony.war.spells;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.tommytony.war.War;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class Barrage extends InstantSpell {

    private static ProjectileListener handler;

    private double velocity;
    private int arrowAmount;
    private double yOffset;

    public Barrage(MagicConfig config, String spellName) {
        super(config, spellName);
        velocity = config.getDouble("velocity", 1.8);
        arrowAmount = config.getInt("amount", 36);
        yOffset = config.getDouble("y-offset", 0);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (handler != null) {
            handler = new ProjectileListener();
        }
    }

    @Override
    public PostCastAction castSpell(Player player, SpellCastState spellCastState, float v, String[] strings) {
        if (spellCastState == SpellCastState.NORMAL) {
            barrage(player);
        }
        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public void turnOff() {
        super.turnOff();
        if (handler == null) {
            return;
        }
        handler.turnOff();
    }

    private void barrage(Player p) {
        double tau = 2 * Math.PI;
        double arc = tau / arrowAmount;
        for (double a = 0; a < tau; a += arc) {
            double x = Math.cos(a);
            double z = Math.sin(a);

            Vector direction = new Vector(x, this.yOffset, z).normalize();
            Arrow arrow = p.getWorld().spawn(p.getEyeLocation(), Arrow.class);
            arrow.setMetadata("barrage", new FixedMetadataValue(War.war, true));
            arrow.setVelocity(direction.multiply(velocity));
            arrow.setShooter(p);
        }
    }

    public class ProjectileListener implements Listener {

        public ProjectileListener() {
            registerEvents();
        }

        @EventHandler
        public void onProjectileHit(ProjectileHitEvent event) {
            Projectile projectile = event.getEntity();
            if (projectile instanceof Arrow) {
                Arrow arrow = (Arrow) projectile;
                if (arrow.hasMetadata("barrage")) {
                    arrow.remove();
                }
            }
        }

        public void turnOff() {
            unregisterEvents();
        }
    }
}
