package com.tommytony.war.job;

import com.tommytony.war.War;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class LogSqlJob extends BukkitRunnable {

    abstract void createTable(Connection conn) throws SQLException;

    abstract void saveRecords(Connection conn) throws SQLException;

    @Override
    public void run() {
        Connection conn = null;
        try {
            conn = War.war.getMysqlConfig().getConnection();
            createTable(conn);
            saveRecords(conn);
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException ex) {
            War.war.getLogger().log(Level.SEVERE, "Failed to insert data", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    War.war.getLogger().warning("Could not close connection");
                }
            }
        }
    }


}
