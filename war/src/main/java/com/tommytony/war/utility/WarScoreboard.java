package com.tommytony.war.utility;

import com.tommytony.war.Team;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
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
        String displayFormat = String.format("&8>> &6&l%s &8<<", zone.getName());
        String displayName = ChatColor.translateAlternateColorCodes('&', displayFormat);
        objective.setDisplayName(displayName);

        String currentTeam = team.getName();

        objective.getScore("").setScore(10);
        // Team name
        String teamName = String.format("&6Team&7: &f%s", currentTeam);
        teamName = ChatColor.translateAlternateColorCodes('&', teamName);
        objective.getScore(teamName).setScore(9);

        // Kill count
        String kills = String.valueOf(zone.getKillCount(player.getName()));
        String killScore = String.format("&6Kills&7: &e%s", kills);
        killScore = ChatColor.translateAlternateColorCodes('&', killScore);
        objective.getScore(killScore).setScore(8);

        // Team points
        String teamPoints = String.format("&6Points&7: &e%s&7/&e%s", team.getPoints(), team.getTeamConfig().resolveInt(TeamConfig.MAXSCORE));
        teamPoints = ChatColor.translateAlternateColorCodes('&', teamPoints);
        objective.getScore(teamPoints).setScore(7);

        // Lifepool
        String teamLives = String.format("&6Lives&7: &e%s&7/&e%s", team.getRemainingLives(), team.getTeamConfig().resolveInt(TeamConfig.LIFEPOOL));
        teamLives = ChatColor.translateAlternateColorCodes('&', teamLives);
        objective.getScore(teamLives).setScore(6);

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
                flagStatus = String.format("&6Flag&7: &l%s%s", format, thief);
            } else {
                flagStatus = "&6Flag&7: &fBase";
                flashed = false;
            }
            flagStatus = ChatColor.translateAlternateColorCodes('&', flagStatus);
            objective.getScore(flagStatus).setScore(5);
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
