package com.tommytony.war.job;

import com.tommytony.war.stats.HealRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class LogHealsJob extends LogSqlJob {

    private List<HealRecord> records;

    public LogHealsJob(List<HealRecord> records) {
        this.records = records;
    }

    @Override
    void createTable(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `heals` (`date` DATETIME NOT NULL, `healer` VARCHAR(16) NOT NULL, `target` VARCHAR(16) NOT NULL, `amount` DOUBLE NOT NULL, KEY `healer` (`healer`)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=latin1");
        statement.close();
    }

    @Override
    void saveRecords(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO heals (date, healer, target, amount) VALUES (NOW(), ?, ?, ?)");
        conn.setAutoCommit(false);
        for (HealRecord record : records) {
            statement.setString(1, record.getHealer());
            statement.setString(2, record.getTarget());
            statement.setDouble(2, record.getAmount());
            statement.addBatch();
        }
        statement.executeBatch();
        statement.close();
    }
}
