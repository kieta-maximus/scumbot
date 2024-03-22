package Parsing;

import SQLConnectionManagement.SQLCommand;
import org.json.JSONObject;

import java.sql.SQLException;

public class WelcomePackJSON {

    public WelcomePackJSON (JSONObject jsonObj) {
            try {
                new SQLCommand().InsertWelcomePack(jsonObj);
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
}
