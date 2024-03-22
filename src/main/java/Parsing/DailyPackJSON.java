package Parsing;

import SQLConnectionManagement.SQLCommand;
import org.json.JSONObject;

import java.sql.SQLException;

public class DailyPackJSON {
    public DailyPackJSON(JSONObject jsonObj) {
        try {
            new SQLCommand().InsertDailyPack(jsonObj);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
