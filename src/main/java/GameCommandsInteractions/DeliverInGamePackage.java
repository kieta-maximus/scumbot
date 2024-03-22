package GameCommandsInteractions;

import Interfaces.SetThreadActive;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

public class DeliverInGamePackage {
    Robot robot;
    Clipboard clipboard;

    public DeliverInGamePackage(String spawn_command, String steam_id, SetThreadActive threadWatch) throws AWTException {
        robot = new Robot();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        threadWatch.setActiveStatus(true);

        JSONArray jsonArray = new JSONArray(spawn_command);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject package_item = jsonArray.getJSONObject(i);
            String pre_command = package_item.getString("Content");
            String command = pre_command + String.format(" Location \"%s\"", steam_id);
            CopyPaste(command);
        }
        threadWatch.setActiveStatus(false);
    }

    private void CopyPaste(String command) {
        try {
            new OpenChatWindow();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        StringSelection stringSelection = new StringSelection(command);
        clipboard.setContents(stringSelection, stringSelection);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyPress(KeyEvent.VK_BACK_SPACE);
        robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
