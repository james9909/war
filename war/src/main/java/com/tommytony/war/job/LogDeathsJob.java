package com.tommytony.war.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class LogDeathsJob extends LogSqlJob {

    private String playerName;
    private int deaths;

    public LogDeathsJob(String playerName, int deaths) {
        this.playerName = playerName;
        this.deaths = deaths;
    }

    @Override
    void createTable(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `deaths` (`player` VARCHAR(16) NOT NULL, `deaths` INT NOT NULL, KEY `player` (`player`)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=latin1");
        statement.close();
    }

    @Override
    void saveRecords(Connection conn) throws SQLException {
        if (deaths > 0) {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO deaths (player, deaths) VALUES (?, ?) ON DUPLICATE KEY UPDATE `deaths` = `deaths` + ?");
            statement.setString(1, playerName);
            statement.setInt(2, deaths);
            statement.setInt(3, deaths);
            statement.execute();
            statement.close();
        }
    }
}
