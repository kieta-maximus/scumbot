package SQLConnectionManagement;

import org.sqlite.SQLiteException;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InitDBStructure {

    // ... create empty conn and statement sql object on global scope
    Connection conn;
    Statement stmt = null;
    String db_location;

    public InitDBStructure() throws SQLException {
        try {
            db_location = new File(SQLCommand.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "\\scum-game-bot.db";
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        // ... create connection instance to sqlite db
        conn = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        createTables();
    }

    private void createTables() throws SQLException {
        String players_sql = """
                CREATE TABLE IF NOT EXISTS players (
                    id              INTEGER       PRIMARY KEY AUTOINCREMENT
                                                  NOT NULL,
                    steam_id        VARCHAR (20)  NOT NULL    UNIQUE,
                    scum_name       VARCHAR (255) NOT NULL,
                    discord_id      VARCHAR (20)  NOT NULL,
                    welcome_pack_id VARCHAR (9)   NOT NULL,
                    admin           BOOLEAN       NOT NULL,
                    player_balance  INTEGER       NOT NULL
                                                  DEFAULT (0),
                    daily_last_time DATETIME
                );""";

        String squads = """
                CREATE TABLE IF NOT EXISTS squads (
                    id                  INTEGER      PRIMARY KEY AUTOINCREMENT
                                                     NOT NULL,
                    name                VARCHAR (65) NOT NULL,
                    owner_steam_id      VARCHAR (65) NOT NULL,
                    active_wars         BOOLEAN      NOT NULL,
                    active_treaties     BOOLEAN      NOT NULL,
                    clothing_pack_id    VARCHAR (510));""";

        String lotto_packs = """
                CREATE TABLE IF NOT EXISTS lotto_packs (
                    id               INTEGER       PRIMARY KEY AUTOINCREMENT,
                    pack_name        VARCHAR (255) NOT NULL,
                    game_command     VARCHAR (2500) NOT NULL,
                    pack_description VARCHAR (255) NOT NULL);""";

        String lotto_results = """
                CREATE TABLE IF NOT EXISTS lotto_results (
                    id            INTEGER       PRIMARY KEY AUTOINCREMENT,
                    discord_id    VARCHAR (20)  NOT NULL,
                    steam_id      VARCHAR (20) NOT NULL,
                    winning_lotto VARCHAR (20)  NOT NULL,
                    claimed       BOOLEAN       NOT NULL);""";

        String welcome_packs = """
                CREATE TABLE IF NOT EXISTS welcome_packs (
                    id           INTEGER    PRIMARY KEY AUTOINCREMENT
                                            NOT NULL,
                    game_command VARCHAR (2500) NOT NULL);""";

        String shop_items = """
                CREATE TABLE IF NOT EXISTS shop_items (
                    id               INTEGER        PRIMARY KEY AUTOINCREMENT
                                                    NOT NULL,
                    channel_id       VARCHAR (20)   NOT NULL
                                                    UNIQUE,
                    item_name        VARCHAR (255)  NOT NULL,
                    price            INTEGER        NOT NULL,
                    img_url          VARCHAR (550)  NOT NULL,
                    game_command     VARCHAR (2500) NOT NULL,
                    item_description VARCHAR (1250) NOT NULL,
                    store_id         VARCHAR (10)   NOT NULL
                );""";

        List<String> tables = new ArrayList<>(); // ... Create list of SQL statements to run
        tables.add(players_sql);
        tables.add(squads);
        tables.add(lotto_packs);
        tables.add(lotto_results);
        tables.add(welcome_packs);
        tables.add(shop_items);

        stmt = conn.createStatement();

        for (String table : tables) {
            try {
                stmt.executeUpdate(table); // ... Execute SQL statements and create tables structure if they don't exist
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        stmt.close();
        conn.close();
    }
}
