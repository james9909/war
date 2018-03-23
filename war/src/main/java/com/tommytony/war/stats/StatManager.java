package com.tommytony.war.stats;

import com.tommytony.war.War;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class StatManager {

    public static boolean initializeTables() {
        return runSql((conn) -> {
            Statement statement = conn.createStatement();
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS players (player VARCHAR(16) NOT NULL, deaths INT DEFAULT 0, wins INT DEFAULT 0, losses INT DEFAULT 0, mvps INT DEFAULT 0, PRIMARY KEY (player)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=latin1");
            statement.execute(
                "CREATE TABLE IF NOT EXISTS heals (date DATETIME NOT NULL, healer VARCHAR(16) NOT NULL, target VARCHAR(16) NOT NULL, amount DOUBLE NOT NULL, KEY (healer)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=latin1");
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS kills (date DATETIME NOT NULL, attacker VARCHAR(16) NOT NULL, defender VARCHAR(16) NOT NULL, KEY (attacker)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=latin1");
            statement.close();
        });
    }

    private static boolean runSql(SqlConsumer<Connection> function) {
        if (!War.war.getMysqlConfig().isEnabled()) {
            return false;
        }
        Connection conn = null;
        boolean success = true;
        try {
            conn = War.war.getMysqlConfig().getConnection();
            function.accept(conn);
        } catch (Exception e) {
            War.war.getLogger().log(Level.SEVERE, "Failed to execute SQL", e);
            success = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    War.war.getLogger().warning("Could not close connection");
                    success = false;
                }
            }
        }
        return success;
    }

    public static boolean resetStats() {
        return runSql((conn) -> {
            Statement drop = conn.createStatement();
            drop.execute("DROP TABLE IF EXISTS kills");
            drop.execute("DROP TABLE IF EXISTS heals");
            drop.execute("DROP TABLE IF EXISTS players");
            drop.close();
        });
    }

    public static boolean addHeal(Player healer, Player target, double amount) {
        return runSql((conn) -> {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO heals (date, healer, target, amount) VALUES (NOW(), ?, ?, ?)");
            statement.setString(1, healer.getName());
            statement.setString(2, target.getName());
            statement.setDouble(3, amount);
            statement.execute();
            statement.close();
        });
    }

    public static boolean addWin(Player player) {
        return runSql((conn -> {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO players (player, wins) VALUES (?, 1) ON DUPLICATE KEY UPDATE `wins` = `wins` + 1");
            statement.setString(1, player.getName());
            statement.executeUpdate();
            statement.close();
        }));
    }

    public static boolean addLoss(Player player) {
        return runSql((conn -> {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO players (player, losses) VALUES (?, 1) ON DUPLICATE KEY UPDATE `losses` = `losses` + 1");
            statement.setString(1, player.getName());
            statement.executeUpdate();
            statement.close();
        }));
    }

    public static boolean addKill(Player attacker, Player defender) {
        return runSql((conn) -> {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO kills (date, attacker, defender) VALUES (NOW(), ?, ?)");
            statement.setString(1, attacker.getName());
            statement.setString(2, defender.getName());
            statement.execute();
            statement.close();
        });
    }

    public static boolean addDeath(Player victim) {
        return runSql((conn) -> {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO players (player, deaths) VALUES (?, 1) ON DUPLICATE KEY UPDATE `deaths` = `deaths` + 1");
            statement.setString(1, victim.getName());
            statement.executeUpdate();
            statement.close();
        });
    }

    public static PlayerStat getStats(Player player) {
        PlayerStat playerStat = new PlayerStat();
        runSql((conn) -> {
            PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM kills WHERE attacker = ?");
            statement.setString(1, player.getName());
            ResultSet result = statement.executeQuery();
            result.next();
            playerStat.setKills(result.getInt(1));

            statement = conn.prepareStatement("SELECT COALESCE(SUM(amount), 0) FROM heals WHERE healer = ?");
            statement.setString(1, player.getName());
            result = statement.executeQuery();
            result.next();
            playerStat.setHeartsHealed(result.getDouble(1));

            statement = conn.prepareStatement("SELECT deaths, wins, losses, mvps FROM players WHERE player = ?");
            statement.setString(1, player.getName());
            result = statement.executeQuery();
            if (result.next()) {
                playerStat.setDeaths(result.getInt(1));
                playerStat.setWins(result.getInt(2));
                playerStat.setLosses(result.getInt(3));
                playerStat.setMvps(result.getInt(4));
            }
        });

        return playerStat;
    }

    @FunctionalInterface
    private interface SqlConsumer<Connection> extends Consumer<Connection> {

        void acceptThrows(Connection conn) throws SQLException;

        @Override
        default void accept(Connection conn) {
            try {
                acceptThrows(conn);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
