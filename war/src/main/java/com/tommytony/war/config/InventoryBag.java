package com.tommytony.war.config;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.utility.Loadout;
import com.tommytony.war.utility.Reward;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryBag {

    private Map<String, Loadout> loadouts = new HashMap<>();
    private Reward winReward = null;
    private Reward lossReward = null;

    private Warzone warzone;

    public InventoryBag(Warzone warzone) {
        this.warzone = warzone;
    }

    public InventoryBag() {
        this.warzone = null;
    }

    public void addLoadout(Loadout newLoadout) {
        String name = newLoadout.getName();
        if (containsLoadout(name)) {
            Loadout loadout = getLoadout(name);
            loadout.setItems(newLoadout.getItems());
            loadout.setHelmet(newLoadout.getHelmet());
            loadout.setChestplate(newLoadout.getChestplate());
            loadout.setLeggings(newLoadout.getLeggings());
            loadout.setBoots(newLoadout.getBoots());
            loadout.setOffhand(newLoadout.getOffhand());
            return;
        }
        this.loadouts.put(name.toLowerCase(), newLoadout);
    }

    public void removeLoadout(String name) {
        loadouts.remove(name.toLowerCase());
    }

    public boolean hasLoadouts() {
        return loadouts.size() > 0;
    }

    public Map<String, Loadout> getLoadouts() {
        return loadouts;
    }

    public void setLoadouts(Map<String, Loadout> loadouts) {
        this.loadouts = loadouts;
    }

    public Map<String, Loadout> resolveLoadouts() {
        if (this.hasLoadouts()) {
            return this.getLoadouts();
        } else if (warzone != null && warzone.getDefaultInventories().hasLoadouts()) {
            return warzone.getDefaultInventories().getLoadouts();
        } else if (War.war.getDefaultInventories().hasLoadouts()) {
            return War.war.getDefaultInventories().getLoadouts();
        } else {
            return new HashMap<>();
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

    public Loadout getLoadout(String loadoutName) {
        return resolveLoadouts().getOrDefault(loadoutName.toLowerCase(), null);
    }

    public boolean containsLoadout(String name) {
        return resolveLoadouts().containsKey(name.toLowerCase());
    }
}
