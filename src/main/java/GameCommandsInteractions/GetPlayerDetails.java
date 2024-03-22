package GameCommandsInteractions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class GetPlayerDetails {
    Robot robot;
    Clipboard clipboard;

    public GetPlayerDetails() throws AWTException {
        robot = new Robot();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    public void SetTPLocation() {
        String command = "#Teleport -255450.9275 -17556.1551 500000";
        CopyPaste(command);
    }

    public JSONArray PlayerDetails() throws AWTException {
        String command = "#ListPlayers true";
        StringSelection stringSelection = new StringSelection("clear"); // ... Set clipboard to static variable to ensure check for bot online
        clipboard.setContents(stringSelection, null);
        CopyPaste(command);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return FormatPlayerDetails();
    }

    // ... List Players in-game and store results in clipboard
    private void CopyPaste(String s) {
        StringSelection stringSelection = new StringSelection(s);
        clipboard.setContents(stringSelection, stringSelection);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    private JSONArray FormatPlayerDetails(){
        try {
            String clipboard_contents = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);

            JSONArray players_array = new JSONArray();
            String[] lines = clipboard_contents.split("\\r?\\n|\\r");
            int sid_position = 2;
            int fame_position = 3;
            int scum_name_position = 1;
            JSONObject player_json = new JSONObject();
            for (int i=0; i<lines.length; i++) {
                /*
                * The format currently looks like this
                *   6. <scum-name>
                *   Steam: <steam-name> (<steam-id>)
                *   Fame: <famepoints>
                */

                if (i==sid_position) {
                    // Get steam_id, get everything between () in the position of sid_position
                    String steam_id = lines[i];
                    steam_id = steam_id.substring(steam_id.indexOf("(") + 1);
                    steam_id = steam_id.substring(0, steam_id.indexOf(")"));
                    player_json.put("steam_id", steam_id);
                    sid_position = i+4;
                }
                if (i==fame_position) {
                    // Get famepoints, get everything past ": " in the position of fame_position
                    String famepoints = lines[i];
                    famepoints = famepoints.substring(famepoints.lastIndexOf(": ") + 1);
                    famepoints = famepoints.replaceAll("\\s", "");
                    player_json.put("fame_points", famepoints);
                    players_array.put(player_json);
                    player_json = new JSONObject();
                    fame_position = i+4;
                }
                if (i==scum_name_position) {
                    // Get scum_name, get everything past ". " in the position of scum_name_position
                    String scum_name = lines[i];
                    scum_name = scum_name.substring(scum_name.lastIndexOf(". ") + 1);
                    scum_name = scum_name.replaceAll("\\s", "");
                    player_json.put("scum_name", scum_name);
                    scum_name_position = i+4;
                }
                else {
                    continue;
                }
            }
            return players_array;

        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
}
