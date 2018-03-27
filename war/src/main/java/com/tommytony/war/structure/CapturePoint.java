package com.tommytony.war.structure;

import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamKind;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.volume.Volume;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

/**
 * Capture points
 *
 * @author Connor Monahan
 */
public class CapturePoint {

    private static int[][] structure = {
        {0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 1, 1, 0, 0},
        {0, 1, 1, 2, 2, 2, 1, 1, 0},
        {0, 1, 2, 2, 2, 2, 2, 1, 0},
        {1, 1, 2, 2, 2, 2, 2, 1, 1},
        {0, 1, 2, 2, 2, 2, 2, 1, 0},
        {0, 1, 1, 2, 2, 2, 1, 1, 0},
        {0, 0, 1, 1, 1, 1, 1, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0},
    };

    private final String name;
    private Volume volume;
    private Location location;
    private TeamKind controller, defaultController;
    private int strength, controlTime;
    private Warzone zone;
    private long lastMessage = 0;

    private Map<Team, Integer> activeTeams;

    public CapturePoint(String name, Location location, TeamKind defaultController, int strength, Warzone warzone) {
        this.name = name;
        this.defaultController = defaultController;
        this.controller = defaultController;
        this.strength = strength;
        this.controlTime = 0;
        this.zone = warzone;
        this.volume = new Volume("cp-" + name, warzone.getWorld());
        this.setLocation(location);

        this.activeTeams = new HashMap<>();
    }

    private Location getOrigin() {
        return location.clone().subtract(structure[0].length / 2, 1, structure.length / 2).getBlock().getLocation();
    }

    private void updateBlocks() {
        Validate.notNull(location);
        // Set origin to back left corner
        Location origin = this.getOrigin();

        // Build structure
        for (int z = 0; z < structure.length; z++) {
            for (int x = 0; x < structure[0].length; x++) {
                BlockState state = origin.clone().add(x, 0, z).getBlock().getState();
                switch (structure[z][x]) {
                    case 0:
                        break;
                    case 1:
                        state.setType(Material.OBSIDIAN);
                        break;
                    case 2:
                        if (this.controller != null) {
                            state.setType(this.controller.getMaterial());
                            state.setData(this.controller.getBlockData());
                        } else {
                            state.setType(Material.DOUBLE_STEP);
                        }
                        break;
                    default:
                        throw new IllegalStateException("Invalid structure");
                }
                state.update(true);
            }
        }
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), 0);
        this.volume.setCornerOne(this.getOrigin());
        this.volume.setCornerTwo(this.getOrigin().add(structure[0].length, 0, structure.length));
        this.volume.saveBlocks();
        this.updateBlocks();
    }

    public TeamKind getDefaultController() {
        return defaultController;
    }

    public TeamKind getController() {
        return controller;
    }

    public void setController(TeamKind controller) {
        this.controller = controller;
        if (strength > 0) {
            this.updateBlocks();
        }
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        Validate.isTrue(strength <= getMaxStrength());
        this.strength = strength;
        if (strength == 0 || strength == getMaxStrength()) {
            this.updateBlocks();
        }
    }

    public int getControlTime() {
        return controlTime;
    }

    public void setControlTime(int controlTime) {
        this.controlTime = controlTime;
    }

    public Volume getVolume() {
        return volume;
    }

    public void setVolume(Volume volume) {
        this.volume = volume;
    }

    public void reset() {
        this.controller = defaultController;
        this.controlTime = 0;
        if (this.controller != null) {
            this.strength = 4;
        } else {
            this.strength = 0;
        }
        this.updateBlocks();
    }

    public boolean antiChatSpam() {
        long now = System.currentTimeMillis();
        if (now - lastMessage > 3000) {
            lastMessage = now;
            return true;
        }
        return false;
    }

    public int getMaxStrength() {
        return zone.getWarzoneConfig().getInt(WarzoneConfig.CAPTUREPOINTTIME);
    }

    private void decrementStrength(Team contesting) {
        if (strength < 1) {
            // strength is already at minimum, ensure attributes are wiped
            setController(null);
            setStrength(0);
            return;
        }

        strength--;
        if (strength == 0) {
            if (antiChatSpam()) {
                zone.broadcast("zone.capturepoint.lose", controller.getFormattedName(), name);
            }
            setControlTime(0);
            setController(null);
        } else if (strength == getMaxStrength() - 1) {
            if (antiChatSpam()) {
                zone.broadcast("zone.capturepoint.contest", name, contesting.getKind().getColor() + contesting.getName() + ChatColor.WHITE);
            }
        }
        setStrength(strength);
    }

    private void decrementStrength(Team contesting, int amount) {
        for (int i = 0; i < amount; i++) {
            if (getStrength() > 0) {
                decrementStrength(contesting);
            } else {
                incrementStrength(contesting);
            }
        }
    }

    private void incrementStrength(Team owner) {
        int maxStrength = getMaxStrength();
        if (strength > maxStrength) {
            // cap strength at CapturePoint.MAX_STRENGTH
            setStrength(maxStrength);
            return;
        } else if (strength == maxStrength) {
            // do nothing
            return;
        }

        strength += 1;
        if (strength == maxStrength) {
            if (antiChatSpam()) {
                zone.broadcast("zone.capturepoint.capture", controller.getFormattedName(), name);
            }
            owner.addPoint();
        } else if (strength == 1) {
            if (antiChatSpam()) {
                zone.broadcast("zone.capturepoint.fortify", owner.getKind().getFormattedName(), name);
            }
            setController(owner.getKind());
        }
        setStrength(strength);
    }

    private void incrementStrength(Team owner, int amount) {
        int maxStrength = getMaxStrength();
        for (int i = 0; i < amount; i++) {
            if (getStrength() < maxStrength) {
                incrementStrength(owner);
            }
        }
    }

    public void addActiveTeam(Team team) {
        activeTeams.put(team, activeTeams.getOrDefault(team, 0)+1);
    }

    public void clearActiveTeams() {
        activeTeams.clear();
    }

    public void calculateStrength() {
        switch (activeTeams.size()) {
            case 0:
                break;
            case 1:
                // Only one team on the point
                Team team = (Team) activeTeams.keySet().toArray()[0];
                if (this.controller == null || this.controller == team.getKind()) {
                    // Take control of neutral point / fortify team point
                    incrementStrength(team, activeTeams.get(team));
                } else {
                    // Contest enemy point
                    decrementStrength(team, activeTeams.get(team));
                }
                break;
            case 2:
                // Two teams on the point
                Team[] teams = (Team[]) activeTeams.keySet().toArray();

                Team dominant = teams[0];
                Team other = teams[1];
                if (activeTeams.get(dominant) < activeTeams.get(other)) {
                    // Swap
                    Team tmp = dominant;
                    dominant = other;
                    other = tmp;
                }

                int dominantCount = activeTeams.get(dominant);
                int otherCount = activeTeams.get(other);
                if (dominantCount == otherCount) {
                    // Both teams have equal presence, so nothing happens
                    break;
                } else {
                    int difference = dominantCount - otherCount;
                    if (this.controller == null || this.controller == dominant.getKind()) {
                        // Take control of neutral point / fortify team point
                        incrementStrength(dominant, difference);
                    } else {
                        // Contest enemy point
                        decrementStrength(dominant, difference);
                    }
                }
                break;
            default:
                // More than 2 teams is not supported
                break;
        }
    }
}
