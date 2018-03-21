package com.tommytony.war.command.admin;

import com.tommytony.war.War;
import com.tommytony.war.command.WarCommandHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
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
        Connection conn = null;
        try {
            conn = War.war.getMysqlConfig().getConnection();
            Statement drop = conn.createStatement();
            drop.execute("DROP TABLE IF EXISTS `kills`");
            drop.execute("DROP TABLE IF EXISTS `heals`");
            drop.execute("DROP TABLE IF EXISTS `deaths`");
            drop.close();
            conn.commit();
            this.msg("Stats cleared!");
        } catch (SQLException e) {
            War.war.getLogger().log(Level.SEVERE, "Failed to drop tables", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    War.war.getLogger().warning("Could not close connection");
                }
            }
        }
        return true;
    }
}
