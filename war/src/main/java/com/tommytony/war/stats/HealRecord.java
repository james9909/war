package com.tommytony.war.stats;

public class HealRecord {

    private String healer;
    private String target;
    private double amount;

    public HealRecord(String healer, String target, double amount) {
        this.healer = healer;
        this.target = target;
        this.amount = amount;
    }

    public String getHealer() {
        return healer;
    }

    public String getTarget() {
        return target;
    }

    public double getAmount() {
        return amount;
    }
}
