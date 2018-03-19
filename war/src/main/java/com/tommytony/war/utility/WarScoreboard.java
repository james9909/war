package com.tommytony.war.utility;

import com.google.common.base.Splitter;
import com.tommytony.war.Team;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class WarScoreboard {

    private static ConcurrentHashMap<String, WarScoreboard> scoreboards = new ConcurrentHashMap<>();

    private Scoreboard scoreboard;
    private Player player;
    private WarPlayer warPlayer;
    private Objective objective;

    private boolean flashed;

    public WarScoreboard(WarPlayer warPlayer) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.player = warPlayer.getPlayer();
        this.warPlayer = warPlayer;
        this.objective = scoreboard.registerNewObjective(player.getName(), "dummy");
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        setTitle(String.format("&8>> &6&l%s &8<<", warPlayer.getZone().getName()));
        WarScoreboard.scoreboards.put(player.getName(), this);
        player.setScoreboard(scoreboard);
    }

    public void update() {
        Warzone zone = warPlayer.getZone();
        addText("", 10);

        // Team name
        Team team = warPlayer.getTeam();
        String currentTeam = warPlayer.getTeam().getName();
        String teamName = String.format("&6Team&7: &f%s", currentTeam);
        addText(teamName, 9);
        player.setScoreboard(scoreboard);

        // Kill count
        int kills = warPlayer.getKills();
        String killScore = String.format("&6Kills&7: &e%d", kills);
        addText(killScore, 8);

        // Team points
        String teamPoints = String.format("&6Points&7: &e%d&7/&e%d", team.getPoints(), team.getTeamConfig().resolveInt(TeamConfig.MAXSCORE));
        addText(teamPoints, 7);

        // Lifepool
        String teamLives = String.format("&6Lives&7: &e%d&7/&e%d", team.getRemainingLives(), team.getTeamConfig().resolveInt(TeamConfig.LIFEPOOL));
        addText(teamLives, 6);

        addText("", 5);

        // Flag status
        if (team.getTeamFlag() != null) {
            // flag status
            String flagStatus = "";
            if (zone.isTeamFlagStolen(team)) {

                // Implement alternating colors
                String format;
                if (flashed) {
                    format = "&8";
                } else {
                    format = "&7";
                }
                flashed = !flashed;

                Map<UUID, Team> thieves = zone.getFlagThieves();
                UUID thiefId = null;
                for (UUID playerId : thieves.keySet()) {
                    if (thieves.get(playerId).getName().equals(team.getName())) {
                        thiefId = playerId;
                        break;
                    }
                }
                if (thiefId != null) {
                    String thiefName = Bukkit.getPlayer(thiefId).getName();
                    if (thiefName.length() > 8) {
                        thiefName = thiefName.substring(0, 8) + "...";
                    }
                    flagStatus = String.format("&6Flag&7: &l%s%s", format, thiefName);
                }
            } else {
                flagStatus = "&6Flag&7: &fBase";
                flashed = false;
            }
            addText(flagStatus, 4);
        }
    }

    public static WarScoreboard getScoreboard(Player player) {
        return scoreboards.getOrDefault(player.getName(), null);
    }

    public static void removeScoreboard(Player player) {
        scoreboards.remove(player.getName());
    }

    public static ConcurrentHashMap<String, WarScoreboard> getScoreboards() {
        return scoreboards;
    }

    private void addText(String text, int number) {
        org.bukkit.scoreboard.Team team;
        team = scoreboard.getTeam(String.valueOf(number));
        if (team == null) {
            team = scoreboard.registerNewTeam(String.valueOf(number));
            team.addEntry(String.valueOf(org.bukkit.ChatColor.values()[number]));
            objective.getScore(String.valueOf(org.bukkit.ChatColor.values()[number])).setScore(number);
        }

        text = ChatColor.translateAlternateColorCodes('&', text);

        // Set prefix
        Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
        String prefix = iterator.next();
        team.setPrefix(prefix);

        // Color behavior adapted from SimpleScoreboard
        if (iterator.hasNext()) {
            String prefixColor = ChatColor.getLastColors(prefix);
            String suffix = iterator.next();

            if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);
                team.setPrefix(prefix);
                prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
                suffix = suffix.substring(1);
            }

            if (prefixColor == null) {
                prefixColor = ChatColor.RESET.toString();
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, (13 - prefixColor.length()));
            }
            suffix = prefixColor + suffix;
            team.setSuffix(suffix);
        } else {
            team.setSuffix("");
        }
    }

    private void setTitle(String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        objective.setDisplayName(text);
    }
}
