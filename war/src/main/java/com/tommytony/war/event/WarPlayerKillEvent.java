package com.tommytony.war.event;

import com.tommytony.war.Warzone;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class WarPlayerKillEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player killer;
    private LivingEntity victim;
    private Warzone zone;
    private DamageCause cause;

    public WarPlayerKillEvent(Warzone zone, Player killer, LivingEntity victim, DamageCause cause) {
        this.zone = zone;
        this.victim = victim;
        this.killer = killer;
        this.cause = cause;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Warzone getZone() {
        return zone;
    }

    public Player getKiller() {
        return killer;
    }

    public DamageCause getCause() {
        return cause;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
