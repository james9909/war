package com.tommytony.war.structure;

import com.tommytony.war.Team;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamKind;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.volume.Volume;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
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
    private TeamKind controller;
    private TeamKind defaultController;
    private TeamKind challenger;
    private int strength, controlTime;
    private Warzone zone;
    private long lastMessage = 0;

    private Map<TeamKind, Integer> activeTeams;

    private SecureRandom random;
    private List<BlockState> neutralBlocks;
    private List<BlockState> coloredBlocks;

    public CapturePoint(String name, Location location, TeamKind defaultController, int strength, Warzone warzone) {
        this.name = name;
        this.defaultController = defaultController;
        this.controller = defaultController;
        this.challenger = null;
        this.strength = strength;
        this.controlTime = 0;
        this.zone = warzone;
        this.volume = new Volume("cp-" + name, warzone.getWorld());

        this.activeTeams = new HashMap<>();
        this.neutralBlocks = new ArrayList<>();
        this.coloredBlocks = new ArrayList<>();
        this.random = new SecureRandom();

        this.setLocation(location);
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
                        state.setType(Material.DOUBLE_STEP);
                        neutralBlocks.add(state);
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

        this.neutralBlocks.clear();
        this.coloredBlocks.clear();
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
    }

    public void setChallenger(TeamKind challenger) {
        this.challenger = challenger;
    }

    public TeamKind getChallenger() {
        return challenger;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        Validate.isTrue(strength <= getMaxStrength());
        this.strength = strength;
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
        this.neutralBlocks.clear();
        this.coloredBlocks.clear();
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

    private BlockState removeRandomBlock(List<BlockState> blocks) {
        if (blocks.size() == 0) {
            return null;
        }

        int size = blocks.size();
        int index = random.nextInt(size);
        return blocks.remove(index);
    }

    private void decrementStrength(TeamKind contester) {
        if (strength < 1) {
            // strength is already at minimum, ensure attributes are wiped
            setController(null);
            setChallenger(null);
            setStrength(0);
            return;
        }

        BlockState target = removeRandomBlock(coloredBlocks);
        if (target != null) {
            target.setType(Material.DOUBLE_STEP);
            target.update(true);
            neutralBlocks.add(target);
        }

        strength--;
        if (strength == 0) {
            if (antiChatSpam() && controller != null) {
                zone.broadcast("zone.capturepoint.lose", controller.getFormattedName(), name);
            }
            setControlTime(0);
            setChallenger(null);
            setController(null);
        } else if (strength == getMaxStrength() - 1) {
            // Capture point is being challenged
            if (antiChatSpam()) {
                zone.broadcast("zone.capturepoint.contest", name, contester.getFormattedName());
            }
            setChallenger(contester);
        } else {
            if (contester == null) {
                setChallenger(null);
            } else {
                setChallenger(contester);
            }
        }
        setStrength(strength);
    }

    private void decrementStrength(TeamKind contester, int amount) {
        for (int i = 0; i < amount; i++) {
            if (getStrength() > 0) {
                decrementStrength(contester);
            } else {
                incrementStrength(contester);
            }
        }
    }

    private void incrementStrength(TeamKind contester) {
        int maxStrength = getMaxStrength();
        if (strength > maxStrength) {
            // cap strength at CapturePoint.MAX_STRENGTH
            setStrength(maxStrength);
            setChallenger(null);
            return;
        } else if (strength == maxStrength) {
            setChallenger(null);
            return;
        }

        BlockState target = removeRandomBlock(neutralBlocks);
        if (target != null) {
            target.setType(contester.getMaterial());
            target.setData(contester.getBlockData());
            target.update(true);
            coloredBlocks.add(target);
        }

        strength++;
        if (strength == maxStrength && controller == null) {
            if (antiChatSpam()) {
                zone.broadcast("zone.capturepoint.capture", contester.getFormattedName(), name);
            }
            setController(contester);
            setChallenger(null);

            Team contesterTeam = zone.getTeamByKind(contester);
            contesterTeam.addPoint();
            contesterTeam.resetSign();
        } else if (strength == 1) {
            // Capture point is being challenged
            if (antiChatSpam()) {
                zone.broadcast("zone.capturepoint.fortify", contester.getFormattedName(), name);
            }
            setChallenger(contester);
        } else {
            if (contester == controller) {
                setChallenger(null);
            } else {
                setChallenger(contester);
            }
        }
        setStrength(strength);
    }

    private void incrementStrength(TeamKind contester, int amount) {
        int maxStrength = getMaxStrength();
        for (int i = 0; i < amount; i++) {
            if (getStrength() < maxStrength) {
                incrementStrength(contester);
            }
        }
    }

    public void addActiveTeam(TeamKind kind) {
        activeTeams.put(kind, activeTeams.getOrDefault(kind, 0)+1);
    }

    public void clearActiveTeams() {
        activeTeams.clear();
    }

    public void calculateStrength() {
        switch (activeTeams.size()) {
            case 0:
                if (controller == null && strength > 0) {
                    // Nobody owns the point, so decrease the strength
                    decrementStrength(null);
                } else if (controller != null && strength < getMaxStrength()) {
                    // Increase strength for the capture point
                    incrementStrength(controller);
                }
                break;
            case 1:
                // Only one team on the point
                TeamKind kind = (TeamKind) activeTeams.keySet().toArray()[0];
                if (this.controller == null || this.controller == kind) {
                    // Take control of neutral point / fortify team point
                    incrementStrength(kind, activeTeams.get(kind));
                } else {
                    // Contest enemy point
                    decrementStrength(kind, activeTeams.get(kind));
                }
                break;
            case 2:
                // Two teams on the point
                List<TeamKind> teams = new ArrayList<>(activeTeams.keySet());

                TeamKind dominant = teams.get(0);
                TeamKind other = teams.get(1);
                if (activeTeams.get(dominant) < activeTeams.get(other)) {
                    // Swap
                    TeamKind tmp = dominant;
                    dominant = other;
                    other = tmp;
                }

                int dominantCount = activeTeams.get(dominant);
                int otherCount = activeTeams.get(other);
                if (dominantCount != otherCount) {
                    int difference = dominantCount - otherCount;
                    if (this.controller == null || this.controller == dominant) {
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
