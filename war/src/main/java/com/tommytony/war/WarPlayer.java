package com.tommytony.war;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class WarPlayer {
    private static HashMap<String, WarPlayer> totalPlayers = new HashMap<>();

    private Player player;
    private Team team;
    private Warzone warzone;

    public WarPlayer(Player player) {
        this.player = player;
        totalPlayers.put(player.getName(), this);
    }

    public Warzone getWarzone() {
        return warzone;
    }

    public void setWarzone(Warzone warzone) {
        this.warzone = warzone;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Player getPlayer() {
        return player;
    }

    public static Set<WarPlayer> getAllPlayers() {
        return new HashSet<>(totalPlayers.values());
    }

    public static WarPlayer getPlayer(String name) {
        return totalPlayers.getOrDefault(name, null);
    }
}
