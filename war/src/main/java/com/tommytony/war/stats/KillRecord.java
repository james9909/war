package com.tommytony.war.stats;

public class KillRecord {

    private String attacker;
    private String defender;

    public KillRecord(String attacker, String defender) {
        this.attacker = attacker;
        this.defender = defender;
    }

    public String getDefender() {
        return defender;
    }

    public String getAttacker() {
        return attacker;
    }
}
