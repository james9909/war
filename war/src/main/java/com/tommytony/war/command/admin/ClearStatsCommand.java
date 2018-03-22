package com.tommytony.war.command.admin;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import com.tommytony.war.stats.StatManager;
import org.bukkit.command.CommandSender;

public class ClearStatsCommand extends AbstractWarAdminCommand {

    public ClearStatsCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotWarAdminException {
        super(handler, sender, args);
    }

    @Override
    public boolean handle() {
        if (!War.war.getMysqlConfig().isEnabled()) {
            this.msg("MySQL is not enabled");
            return true;
        }
        if (StatManager.resetStats() && StatManager.initializeTables()) {
            this.msg("Stats reset!");
        } else {
            this.msg("Failed to reset stats");
        }
        return true;
    }
}
