package com.tommytony.war.utility;

public class LoadoutSelection {

    private boolean stillInSpawn;
    private int selectedLoadout;

    public LoadoutSelection(boolean stillInSpawn, int selectedLoadout) {
        this.stillInSpawn = stillInSpawn;
        this.selectedLoadout = selectedLoadout;
    }

    public boolean isStillInSpawn() {
        return stillInSpawn;
    }

    public void setStillInSpawn(boolean stillInSpawn) {
        this.stillInSpawn = stillInSpawn;
    }

    public int getSelectedLoadout() {
        return selectedLoadout;
    }

    public void setSelectedLoadout(int selectedLoadout) {
        this.selectedLoadout = selectedLoadout;
    }
}
