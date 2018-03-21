package com.tommytony.war.stats;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.War;
import com.tommytony.war.job.LogDeathsJob;
import com.tommytony.war.job.LogHealsJob;
import com.tommytony.war.job.LogKillsJob;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public class StatTracker {

    private String playerName;
    private List<KillRecord> kills;
    private List<HealRecord> heals;
    private int deaths;

    public StatTracker(String playerName) {
        this.playerName = playerName;
        kills = new ArrayList<>();
        heals = new ArrayList<>();
        deaths = 0;
    }

    public void addHeal(Player target, double hearts) {
        heals.add(new HealRecord(playerName, target.getName(), hearts));
    }

    public void addKill(Player defender) {
        kills.add(new KillRecord(playerName, defender.getName()));
    }

    public void addDeath() {
        deaths++;
    }

    public void reset() {
        kills.clear();
        heals.clear();
        deaths = 0;
    }

    public void save() {
        if (War.war.getMysqlConfig().isEnabled()) {
            LogKillsJob logKillsJob = new LogKillsJob(ImmutableList.copyOf(kills));
            logKillsJob.runTaskAsynchronously(War.war);

            LogHealsJob logHealsJob = new LogHealsJob(ImmutableList.copyOf(heals));
            logHealsJob.runTaskAsynchronously(War.war);

            LogDeathsJob logDeathsJob = new LogDeathsJob(playerName, deaths);
            logDeathsJob.runTaskAsynchronously(War.war);
        }
    }
}
