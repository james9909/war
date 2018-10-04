package com.tommytony.war.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarPlayerChooseClassEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String className;

    public WarPlayerChooseClassEvent(Player player, String className) {
        this.player = player;
        this.className = className;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getClassName() {
        return className;
    }

    public Player getPlayer() {
        return player;
    }
}
