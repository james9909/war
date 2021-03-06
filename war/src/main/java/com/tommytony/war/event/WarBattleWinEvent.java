package com.tommytony.war.event;

import com.tommytony.war.Team;
import com.tommytony.war.Warzone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class WarBattleWinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private List<Team> winningTeams;
    private Warzone zone;

    public WarBattleWinEvent(Warzone zone, List<Team> winningTeams) {
        this.zone = zone;
        this.winningTeams = winningTeams;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Warzone getZone() {
        return zone;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public List<Team> getWinningTeams() {
        return winningTeams;
    }
}
