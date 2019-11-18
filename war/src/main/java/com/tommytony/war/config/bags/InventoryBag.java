package com.tommytony.war.config.bags;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.utility.Reward;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class InventoryBag {

    private Set<String> loadouts = new TreeSet<>();
    private Reward winReward = null;
    private Reward lossReward = null;

    private Warzone warzone;

    public InventoryBag(Warzone warzone) {
        this.warzone = warzone;
    }

    public InventoryBag() {
        this.warzone = null;
    }

    public void addLoadout(String name) {
        this.loadouts.add(name.toLowerCase());
    }

    public void removeLoadout(String name) {
        loadouts.remove(name.toLowerCase());
    }

    public boolean hasLoadouts() {
        return loadouts.size() > 0;
    }

    public Set<String> getLoadouts() {
        return loadouts;
    }

    public void setLoadouts(Set<String> loadouts) {
        this.loadouts = loadouts;
    }

    public Set<String> resolveLoadouts() {
        if (this.hasLoadouts()) {
            return this.getLoadouts();
        } else if (warzone != null && warzone.getDefaultInventories().hasLoadouts()) {
            return warzone.getDefaultInventories().getLoadouts();
        } else if (War.war.getDefaultInventories().hasLoadouts()) {
            return War.war.getDefaultInventories().getLoadouts();
        } else {
            return new HashSet<>();
        }
    }

    public boolean hasWinReward() {
        return winReward != null && winReward.hasRewards();
    }

    public Reward getWinReward() {
        return winReward;
    }

    public void setWinReward(Reward winReward) {
        this.winReward = winReward;
    }

    public Reward resolveWinReward() {
        if (this.hasWinReward()) {
            return winReward;
        } else if (warzone != null && warzone.getDefaultInventories().hasWinReward()) {
            return warzone.getDefaultInventories().getWinReward();
        } else if (War.war.getDefaultInventories().hasWinReward()) {
            return War.war.getDefaultInventories().getWinReward();
        } else {
            return new Reward();
        }
    }

    public boolean hasLossReward() {
        return lossReward != null && lossReward.hasRewards();
    }

    public Reward getLossReward() {
        return lossReward;
    }

    public void setLossReward(Reward lossReward) {
        this.lossReward = lossReward;
    }

    public Reward resolveLossReward() {
        if (this.hasLossReward()) {
            return lossReward;
        } else if (warzone != null && warzone.getDefaultInventories().hasLossReward()) {
            return warzone.getDefaultInventories().getLossReward();
        } else if (War.war.getDefaultInventories().hasLossReward()) {
            return War.war.getDefaultInventories().getLossReward();
        } else {
            return new Reward();
        }
    }

    public void clearLoadouts() {
        this.loadouts.clear();
    }

    public boolean containsLoadout(String name) {
        return resolveLoadouts().contains(name.toLowerCase());
    }
}
