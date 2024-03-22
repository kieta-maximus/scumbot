package SQLConnectionManagement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sqlite.util.StringUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Date;
import java.util.List;

public class SQLCommand {
    // ... manage all SQL commands, table structure can be found in /java/SQLConnectionManagement/InitDBStructure.java\
    String db_location;
    public SQLCommand() {
        try {
            db_location = new File(SQLCommand.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "\\scum-game-bot.db";
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public void InsertWelcomePack(JSONObject jsonObject) throws SQLException {
        JSONObject outerJSON = jsonObject.getJSONObject("");
        JSONArray jsonArrayContents = outerJSON.getJSONArray("Contents");

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "INSERT INTO welcome_packs (game_command) VALUES (?);";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, jsonArrayContents.toString());

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }

    public void InsertLottoPack(JSONObject jsonObject) throws SQLException {
        JSONObject outerJSON = jsonObject.getJSONObject("");
        JSONArray jsonArrayContents = outerJSON.getJSONArray("Contents");
        String Name = outerJSON.getString("Name");
        String Description = outerJSON.getString("Description");
        String ImageURL = outerJSON.getString("ImageURL");

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "INSERT INTO lotto_packs (pack_name, game_command, pack_description) VALUES (?,?,?);";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, Name);
        pstmt.setString(2, jsonArrayContents.toString());
        pstmt.setString(3, Description);

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }

    public void InsertDailyPack(JSONObject jsonObject) throws SQLException {
        JSONObject outerJSON = jsonObject.getJSONObject("");
        JSONArray jsonArrayContents = outerJSON.getJSONArray("Contents");

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "INSERT INTO daily_packs (game_command) VALUES (?);";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, jsonArrayContents.toString());

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }

    public JSONObject SelectSinglePlayerDb(String type, String id) throws SQLException {
        // ... Find single player from players table, return JSON Obj representing Player or empty JSON obj

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM players WHERE %s = '%s';", type, id));
        JSONObject player_json = new JSONObject();

        while (rs.next()) {
            String scum_name = rs.getString("scum_name");
            String steam_id = rs.getString("steam_id");
            String discord_id = rs.getString("discord_id");
            player_json.put("scum_name", scum_name);
            player_json.put("steam_id", steam_id);
            player_json.put("discord_id", discord_id);
        }
        rs.close();
        stmt.close();
        connection.close();
        return player_json;
    }

    public boolean InsertSinglePlayerDb(String scum_name, String discord_id, String welcome_pack_code, String steam_id) throws SQLException {
        // ... Register (INSERT) a new user to the Players DB

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "INSERT INTO players (steam_id, scum_name, discord_id, welcome_pack_id, admin, player_balance) VALUES (?, ?, ?, ?, false, 0);";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, steam_id);
        pstmt.setString(2, scum_name);
        pstmt.setString(3, discord_id);
        pstmt.setString(4, welcome_pack_code);

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
        return true;
    }

    public String SelectRandomWelcomePack() throws SQLException {
        // ... Select a welcome pack at random
        String returned_string = "[]";
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "SELECT * FROM welcome_packs ORDER BY RANDOM() LIMIT 1;";
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            returned_string = rs.getString("game_command");
        }
        stmt.close();
        connection.close();
        rs.close();
        return returned_string;
    }

    public JSONObject SelectRandomLottoPack() throws SQLException {
        // ... Select a lottery pack at random
        JSONObject json_obj = new JSONObject();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "SELECT * FROM lotto_packs ORDER BY RANDOM() LIMIT 1";
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            json_obj.put("game_command", rs.getString("game_command"));
            json_obj.put("pack_name", rs.getString("pack_name"));
            json_obj.put("pack_description", rs.getString("pack_description"));
        }
        stmt.close();
        rs.close();
        connection.close();
        return json_obj;
    }

    public JSONObject SelectRandomDailyPack() throws SQLException {
        // ... Select a lottery pack at random
        JSONObject json_obj = new JSONObject();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "SELECT * FROM daily_packs ORDER BY RANDOM() LIMIT 1";
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            json_obj.put("game_command", rs.getString("game_command"));
        }
        stmt.close();
        rs.close();
        connection.close();
        return json_obj;
    }

    public void InsertLotteryResults(String discord_id, String steam_id, int winning_lotto) throws SQLException {
        // ... Register (INSERT) a new lotto result to the lotto_results DB

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "INSERT INTO lotto_results (discord_id, steam_id, winning_lotto, claimed) VALUES (?, ?, ?, false);";
        System.out.println("Inserting SQL for lotto: " + sql);

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, discord_id);
        pstmt.setString(2, steam_id);
        pstmt.setString(3, Integer.toString(winning_lotto)); // ... this is the winning lotto number

        int insert_cnt = pstmt.executeUpdate(); // ... pstmt will return 0 or greater than 0 representing the amount of rows inserted
        System.out.println("Inserted " + insert_cnt + " rows into lotto SQL");
        pstmt.close();
        connection.close();
    }

    public String SelectLotteryClaim(String lotto_code, String discord_id) throws SQLException{
        // ... Find lottery result where person has claimed in JDA
        String steam_id = null;

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "SELECT * FROM lotto_results WHERE winning_lotto = ? AND claimed = false AND discord_id = ?;";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, lotto_code);
        pstmt.setString(2, discord_id);

        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            steam_id = rs.getString("steam_id");
        }
        return steam_id;
    }

    public JSONObject SelectRandomLottoWinner(List<String> list) throws SQLException{
        // ... Select 1 random user, using list of strings containing Steam_id's
        // ... SQL query could be vulnerable to SQL injection .. string is not escaped, however, this does not take a user input

        JSONObject return_json = new JSONObject();

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "SELECT * FROM players WHERE steam_id in ("+ StringUtils.join(list, ",")+") ORDER BY RANDOM() LIMIT 1;";
        System.out.println("Finding lotto winner SQL: " + sql);

        Statement stmt = connection.createStatement();
        System.out.println("Found a lotto winner...");

        ResultSet rs = stmt.executeQuery(sql);
        System.out.println("Reviewing lotto winner ResultSet");

        while (rs.next()) {
            String systemMessage = String.format("Looping RS - SteamID: %s\rScumName: %s\rDiscordId: %s", rs.getString("steam_id"), rs.getString("scum_name"), rs.getString("discord_id"));
            System.out.println(systemMessage);
            return_json.put("steam_id", rs.getString("steam_id"));
            return_json.put("scum_name", rs.getString("scum_name"));
            return_json.put("discord_id", rs.getString("discord_id"));
        }

        stmt.close();
        rs.close();
        connection.close();
        return return_json;
    }

    public void UpdateClaimedLotto(String discord_id, String lotto_num) throws SQLException {
        // ... Update lotto entry and set to claimed
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "UPDATE lotto_results SET claimed = true WHERE winning_lotto = ?";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, lotto_num);

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }

    public void UpdateDailyTime(String steam_id, int reward) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        Date date = new java.util.Date();
        Object ts = new Timestamp(date.getTime());
        String sql = "UPDATE players SET player_balance = ISNULL(player_balance, 0) + ? AND daily_last_time = ? WHERE steam_id = ?;";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, reward);
        pstmt.setObject(2, ts);
        pstmt.setString(3, steam_id);

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }

    public void InsertShopItem(String itemName, String itemPrice, String imageURL, String channel, String contents, String storeID) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "INSERT INTO shop_items (" +
                "channel_id, item_name, price, img_url, game_command, store_id) VALUES (" +
                "?,?,?,?,?,?);";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, channel);
        pstmt.setString(2, itemName);
        pstmt.setString(3, itemPrice);
        pstmt.setString(4, imageURL);
        pstmt.setString(5, contents);
        pstmt.setString(6, storeID);

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }

    public JSONObject CheckPlayerBalance(String discord_id) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "SELECT player_balance, steam_id FROM players WHERE discord_id = ?;";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, discord_id);
        ResultSet rs = pstmt.executeQuery();

        JSONObject returnJSON = new JSONObject();
        returnJSON.put("player_balance", rs.getInt("player_balance"));
        returnJSON.put("steam_id", rs.getString("steam_id"));

        pstmt.close();
        connection.close();

        return returnJSON;
    }

    public JSONObject SelectShopItem(String item_id) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "SELECT price, game_command FROM shop_items WHERE store_id = ?;";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, item_id);
        ResultSet rs = pstmt.executeQuery();

        JSONObject returnJSON = new JSONObject();
        returnJSON.put("price", rs.getInt("price"));
        returnJSON.put("commands", rs.getString("game_command"));

        pstmt.close();
        connection.close();

        return returnJSON;
    }

    public void UpdatePlayerBalance(int newBalance, String discord_id) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "UPDATE players SET player_balance = ? WHERE discord_id =?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, newBalance);
        pstmt.setString(2, discord_id);

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }

    public void InsertNewDailyPack(String contents) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_location);
        String sql = "INSERT INTO daily_packs (game_command) VALUES (?);";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, contents);

        pstmt.executeUpdate();
        pstmt.close();
        connection.close();
    }
}
