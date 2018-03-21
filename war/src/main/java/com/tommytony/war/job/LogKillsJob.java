package com.tommytony.war.job;

import com.tommytony.war.stats.KillRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class LogKillsJob extends LogSqlJob {

    private List<KillRecord> records;

    public LogKillsJob(List<KillRecord> records) {
        this.records = records;
    }

    @Override
    void createTable(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `kills` (`date` DATETIME NOT NULL, `attacker` VARCHAR(16) NOT NULL, `defender` VARCHAR(16) NOT NULL, KEY `attacker` (`attacker`)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=latin1");
        statement.close();
    }

    @Override
    void saveRecords(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO kills (date, attacker, defender) VALUES (NOW(), ?, ?)");
        conn.setAutoCommit(false);
        for (KillRecord record : records) {
            statement.setString(1, record.getAttacker());
            statement.setString(2, record.getDefender());
            statement.addBatch();
        }
        statement.executeBatch();
        statement.close();
    }
}
