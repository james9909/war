package com.tommytony.war.command.regular;

import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.stats.PlayerStat;
import com.tommytony.war.stats.StatManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand extends AbstractWarCommand {

    public StatsCommand(WarCommandHandler handler, CommandSender sender, String[] args) {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        CommandSender sender = this.getSender();
        if (!(sender instanceof Player)) {
            this.badMsg("command.console");
            return true;
        }

        Player player = (Player) sender;
        PlayerStat stats = StatManager.getStats(player);

        String output = "Stats for " + player.getName() + ":\n";
        output += "Wins: " + stats.getWins() + "\n";
        output += "Losses: " + stats.getLosses() + "\n";
        output += "Kills: " + stats.getKills() + "\n";
        output += "Deaths: " + stats.getDeaths() + "\n";
        output += "Hearts Healed: " + stats.getHeartsHealed() + "\n";
        output += "MVPs: " + stats.getMvps() + "\n";
        this.msg(output);
        return true;
    }
}
