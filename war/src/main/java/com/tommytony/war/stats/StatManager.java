package com.tommytony.war.stats;

import com.tommytony.war.War;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
                "CREATE TABLE IF NOT EXISTS heals (date DATETIME NOT NULL, healer VARCHAR(16) NOT NULL, target VARCHAR(16) NOT NULL, amount DOUBLE NOT NULL, PRIMARY KEY (healer)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=latin1");
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS kills (date DATETIME NOT NULL, attacker VARCHAR(16) NOT NULL, defender VARCHAR(16) NOT NULL, PRIMARY KEY (attacker)) "
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
        return runSql(conn -> {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO players (player, deaths) VALUES (?, 1) ON DUPLICATE KEY UPDATE `deaths` = `deaths` + 1");
            statement.setString(1, victim.getName());
            statement.executeUpdate();
            statement.close();
        });
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
