package com.tommytony.war.utility;

public class LoadoutSelection {

    private boolean stillInSpawn;
    private int selectedIndex;

    public LoadoutSelection(boolean stillInSpawn, int selectedIndex) {
        this.stillInSpawn = stillInSpawn;
        this.selectedIndex = selectedIndex;

    }

    public boolean isStillInSpawn() {
        return stillInSpawn;
    }

    public void setStillInSpawn(boolean stillInSpawn) {
        this.stillInSpawn = stillInSpawn;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
}
