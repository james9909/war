package com.tommytony.war.event;

import com.tommytony.war.Warzone;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarPlayerJoinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private Warzone zone;

    public WarPlayerJoinEvent(Player player, Warzone zone) {
        this.player = player;
        this.zone = zone;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Warzone getZone() {
        return zone;
    }
}
