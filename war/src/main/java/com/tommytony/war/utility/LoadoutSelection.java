package com.tommytony.war.utility;

public class LoadoutSelection {

    private boolean stillInSpawn;
    private String selectedLoadout;

    public LoadoutSelection(boolean stillInSpawn, String selectedLoadout) {
        this.stillInSpawn = stillInSpawn;
        this.selectedLoadout = selectedLoadout;
    }

    public boolean isStillInSpawn() {
        return stillInSpawn;
    }

    public void setStillInSpawn(boolean stillInSpawn) {
        this.stillInSpawn = stillInSpawn;
    }

    public String getSelectedLoadout() {
        return selectedLoadout;
    }

    public void setSelectedLoadout(String selectedLoadout) {
        this.selectedLoadout = selectedLoadout;
    }
}
