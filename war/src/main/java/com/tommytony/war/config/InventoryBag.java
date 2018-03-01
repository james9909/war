package com.tommytony.war.config;

import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.utility.Loadout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class InventoryBag {

    private List<Loadout> loadouts = new ArrayList<>();
    private HashMap<Integer, ItemStack> reward = null;

    private Warzone warzone;

    public InventoryBag(Warzone warzone) {
        this.warzone = warzone;
    }

    public InventoryBag() {
        this.warzone = null;
    }

    public void addLoadout(String name, Chest loadoutChest) {
        this.loadouts.add(new Loadout(name, loadoutChest));
    }

    public void addLoadout(Loadout loadout) {
        this.loadouts.add(loadout);
    }

    public void removeLoadout(String name) {
        ArrayList<Loadout> loadoutsToRemove = new ArrayList<>();
        for (Loadout ldt : loadouts) {
            if (ldt.getName().equals(name)) {
                loadoutsToRemove.add(ldt);
            }
        }

        // avoid concurrent modif exceptions
        for (Loadout loadoutToRemove : loadoutsToRemove) {
            this.removeLoadout(loadoutToRemove);
        }
    }

    public void removeLoadout(Loadout ldt) {
        this.loadouts.remove(ldt);
    }

    public boolean hasLoadouts() {
        return loadouts.size() > 0;
    }

    public List<Loadout> getLoadouts() {
        return loadouts;
    }

    public void setLoadouts(List<Loadout> loadouts) {
        this.loadouts = loadouts;
    }

    public List<Loadout> getNewLoadouts() {
        return loadouts;
    }

    public List<Loadout> resolveLoadouts() {
        if (this.hasLoadouts()) {
            return this.getLoadouts();
        } else if (warzone != null && warzone.getDefaultInventories().hasLoadouts()) {
            return warzone.getDefaultInventories().resolveLoadouts();
        } else if (War.war.getDefaultInventories().hasLoadouts()) {
            return War.war.getDefaultInventories().resolveLoadouts();
        } else {
            return new ArrayList<>();
        }
    }

    public List<Loadout> resolveNewLoadouts() {
        if (this.hasLoadouts()) {
            return this.getNewLoadouts();
        } else if (warzone != null && warzone.getDefaultInventories().hasLoadouts()) {
            return warzone.getDefaultInventories().resolveNewLoadouts();
        } else if (War.war.getDefaultInventories().hasLoadouts()) {
            return War.war.getDefaultInventories().resolveNewLoadouts();
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasReward() {
        return reward != null;
    }

    public HashMap<Integer, ItemStack> getReward() {
        return reward;
    }

    public void setReward(HashMap<Integer, ItemStack> reward) {
        this.reward = reward;
    }

    public HashMap<Integer, ItemStack> resolveReward() {
        if (this.hasReward()) {
            return reward;
        } else if (warzone != null && warzone.getDefaultInventories().hasReward()) {
            return warzone.getDefaultInventories().resolveReward();
        } else {
            return War.war.getDefaultInventories().resolveReward();
        }
    }

    public void clearLoadouts() {
        this.loadouts.clear();
    }

    public Loadout getLoadout(String loadoutName) {
        for (Loadout loadout : loadouts) {
            if (loadout.getName().equals(loadoutName)) {
                return loadout;
            }
        }
        return null;
    }

    public Loadout getNewLoadout(String loadoutName) {
        for (Loadout ldt : loadouts) {
            if (ldt.getName().equals(loadoutName)) {
                return ldt;
            }
        }
        return null;
    }

    public void setLoadout(String name, Chest loadoutChest) {
        for (Loadout loadout : loadouts) {
            if (loadout.getName().equals(name)) {
                loadout.setLoadoutChest(loadoutChest);
                return;
            }
        }
        loadouts.add(new Loadout(name, loadoutChest));
    }

    public boolean containsLoadout(String name) {
        return this.getNewLoadout(name) != null;
    }
}
