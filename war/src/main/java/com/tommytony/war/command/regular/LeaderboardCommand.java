package com.tommytony.war.command.regular;

import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.stats.StatManager;
import com.tommytony.war.utility.TableGenerator;
import org.bukkit.command.CommandSender;

import java.util.*;

public class LeaderboardCommand extends AbstractWarCommand {

    public LeaderboardCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        String className = "Sorcerer";
        if (args.length != 0) {
            className = args[0];
        }

        List<ArrayList<String>> stats = StatManager.getLeaderboardStats(className);
        TableGenerator tg = new TableGenerator(TableGenerator.Alignment.LEFT, TableGenerator.Alignment.LEFT, TableGenerator.Alignment.LEFT);
        tg.addRow("Kills:", "K/D:", "Heals:");

        ArrayList<String> row = new ArrayList<>();
        boolean display = false;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 3; j++) {
                if (i < stats.get(j).size()) {
                    row.add(stats.get(j).get(i));
                    display = true;
                } else {
                    row.add("");
                }
            }
            if (display) {
                tg.addRow(row.get(0), row.get(1), row.get(2));
            }
            display = false;
            row.clear();
        }
        List<String> lines = tg.generate(TableGenerator.Receiver.CLIENT, true, true);
        StringBuilder sb = new StringBuilder();
        sb.append("Stats for ").append(className).append(":\n");
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        this.msg(sb.toString());
        return true;
    }
}