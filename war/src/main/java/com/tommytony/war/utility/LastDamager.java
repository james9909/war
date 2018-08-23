package com.tommytony.war.utility;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LastDamager {
    private static int SECONDS_TIL_EXPIRATION = 3;

    private Player attacker;
    private long timestamp;
    private Entity damager;

    public LastDamager() {
        this.timestamp = 0;
    }

    public boolean isValid() {
        return attacker != null && (this.timestamp / 1000000000.0) > SECONDS_TIL_EXPIRATION;
    }

    public Player getAttacker() {
        return attacker;
    }

    public Entity getDamager() {
        return damager;
    }

    public void setAttacker(Player attacker, Entity damager) {
        this.attacker = attacker;
        this.timestamp = System.nanoTime();
        this.damager = damager;
    }
}
