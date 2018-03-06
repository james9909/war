package com.tommytony.war.utility;

import com.tommytony.war.Team;
import com.tommytony.war.Warzone;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class WarScoreboard {

    private static HashMap<String, WarScoreboard> scoreboards = new HashMap<>();

    private Scoreboard scoreboard;
    private Player player;
    private Team team;
    private Objective objective;

    private boolean flashed;

    public WarScoreboard(Player player, Team team) {
        if (scoreboards.containsKey(player.getName())) {
            this.team = team;
        } else {
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            this.player = player;
            this.team = team;
            this.objective = scoreboard.registerNewObjective(player.getName(), "dummy");

            WarScoreboard.scoreboards.put(player.getName(), this);
        }
    }

    public void update() {
        objective.unregister();
        objective = scoreboard.registerNewObjective(player.getName(), "dummy");
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Warzone zone = team.getZone();
        String displayFormat = String.format("&4> &c&l%s &4<", zone.getName());
        String displayName = ChatColor.translateAlternateColorCodes('&', displayFormat);
        objective.setDisplayName(displayName);

        String currentTeam = team.getKind().getColor() + team.getName();

        objective.getScore("").setScore(10);
        // Team name
        String teamScore = String.format("Team: %s", ChatColor.BOLD + currentTeam);
        objective.getScore(teamScore).setScore(9);

        // Kill count
        String kills = String.valueOf(zone.getKillCount(player.getName()));
        String killScore = String.format("Kills: %s", ChatColor.GREEN + kills);
        objective.getScore(killScore).setScore(8);

        // objective.getScore(" ").setScore(7);
        if (team.getTeamFlag() != null) {
            // flag status
            String flagStatus;
            if (zone.isTeamFlagStolen(team)) {

                // Implement alternating colors
                String format;
                if (flashed) {
                    format = "&8";
                } else {
                    format = "&7";
                }
                flashed = !flashed;

                HashMap<String, Team> thieves = zone.getFlagThieves();
                String thief = "nobody";
                for (String playerName : thieves.keySet()) {
                    if (thieves.get(playerName).getName().equals(team.getName())) {
                        thief = playerName;
                        break;
                    }
                }
                if (thief.length() > 8) {
                    thief = thief.substring(0, 8) + "...";
                }
                flagStatus = String.format("Flag: &l%s%s", format, thief);
            } else {
                flagStatus = "Flag: &aBase";
                flashed = false;
            }
            flagStatus = ChatColor.translateAlternateColorCodes('&', flagStatus);
            objective.getScore(flagStatus).setScore(7);
        }

        player.setScoreboard(scoreboard);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public static WarScoreboard getScoreboard(Player player) {
        return scoreboards.getOrDefault(player.getName(), null);
    }

    public static void removeScoreboard(Player player) {
        scoreboards.remove(player.getName());
    }

    public static HashMap<String, WarScoreboard> getScoreboards() {
        return scoreboards;
    }
}
