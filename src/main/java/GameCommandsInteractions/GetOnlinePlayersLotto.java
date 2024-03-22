package GameCommandsInteractions;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetOnlinePlayersLotto {
    Clipboard clipboard;
    Robot robot;
    public GetOnlinePlayersLotto () throws AWTException {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        robot = new Robot();
    }
    public List<String> PlayerDetails() throws AWTException {
        String command = "#ListPlayers true";
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
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    private List<String> FormatPlayerDetails(){
        List<String> players_array = new ArrayList<String>();
        try {
            String clipboard = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);

            String[] lines = clipboard.split("\\r?\\n|\\r");
            int sid_position = 2;
            for (int i=0; i<lines.length; i++) {
                if (i==sid_position) {
                    // Get steam_id, get everything between () in the position of sid_position
                    String steam_id = lines[i];
                    steam_id = steam_id.substring(steam_id.indexOf("(") + 1);
                    steam_id = steam_id.substring(0, steam_id.indexOf(")"));
                    players_array.add(steam_id);
                    sid_position = i+4;
                }
            }

            return players_array;
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
            return players_array;
        }
    }
}
