package com.tommytony.war.event;

import com.tommytony.war.Team;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarScoreCapEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private List<Team> winningTeams;

    public WarScoreCapEvent(List<Team> winningTeams) {
        this.winningTeams = winningTeams;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public List<Team> getWinningTeams() {
        return winningTeams;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
