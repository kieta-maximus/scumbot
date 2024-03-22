package Parsing;

import SQLConnectionManagement.SQLCommand;
import org.json.JSONObject;

import java.sql.SQLException;

public class LottoPackJSON {

    public LottoPackJSON(JSONObject jsonObj) {
            try {
                new SQLCommand().InsertLottoPack(jsonObj);
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
}
