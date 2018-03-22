package com.tommytony.war.stats;

public class PlayerStat {

    private int wins;
    private int losses;
    private double heartsHealed;
    private int kills;
    private int deaths;
    private int mvps;

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public double getHeartsHealed() {
        return heartsHealed;
    }

    public void setHeartsHealed(double heartsHealed) {
        this.heartsHealed = heartsHealed;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getMvps() {
        return mvps;
    }

    public void setMvps(int mvps) {
        this.mvps = mvps;
    }
}
