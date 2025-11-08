package jp.jyn.mecon.db.driver;

import com.zaxxer.hikari.HikariDataSource;
import jp.jyn.mecon.Mecon;
import jp.jyn.mecon.db.DBMigrationUtils;
import jp.jyn.mecon.db.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SQLite extends Database {
    public SQLite(HikariDataSource hikari) {
        super(hikari);
    }

    @Override
    protected void createTable() {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `account` (" +
                    "`id`   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "`uuid` BLOB    NOT NULL UNIQUE," +
                    "`name` TEXT" +
                    ")"
            );
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `balance` (" +
                    "`id`      INTEGER NOT NULL PRIMARY KEY," +
                    "`balance` INTEGER NOT NULL" +
                    ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void migration() {
        String version = DBMigrationUtils.getVersion(hikari);

        if (version.equals(DBMigrationUtils.CURRENT_VERSION)) {
            return;
        }

        Logger logger = Mecon.getInstance().getLogger();
        logger.info("Migrate SQLite from version " + version);

        if ("1".equals(version)) {
            v1to2();
            version = "2";
        }

        if ("2".equals(version)) {
            try (Connection connection = hikari.getConnection()) {
                DBMigrationUtils.v2to3(connection);
                logger.info("Migration to version 3 completed");
            } catch (SQLException e) {
                logger.severe("Migration from version 2 to 3 failed");
                throw new RuntimeException(e);
            }
        } else if (!version.equals(DBMigrationUtils.CURRENT_VERSION)) {
            logger.severe(DBMigrationUtils.MIGRATION_ERROR_1);
            logger.severe(DBMigrationUtils.MIGRATION_ERROR_2);
            throw new IllegalStateException(String.format(DBMigrationUtils.MIGRATION_EXCEPTION, version));
        }
    }

    private void v1to2() {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            // rename table
            statement.executeUpdate("ALTER TABLE `account` RENAME TO `account_old`");
            statement.executeUpdate("ALTER TABLE `balance` RENAME TO `balance_old`");
            statement.executeUpdate("DROP INDEX `nameindex`");

            // data copy
            createTable();
            DBMigrationUtils.v1copy2(connection);

            // update version
            statement.executeUpdate("DROP TABLE `meta`");
            DBMigrationUtils.getVersion(hikari);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
