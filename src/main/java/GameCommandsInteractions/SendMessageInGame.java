package GameCommandsInteractions;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

public class SendMessageInGame {
    String message;
    Robot robot;
    Clipboard clipboard;

    public SendMessageInGame(String message) throws AWTException {
        this.message = message;
        robot = new Robot();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        SendMessage(message);
    }
    private void SendMessage(String command) {
        StringSelection stringSelection = new StringSelection(command);
        clipboard.setContents(stringSelection, stringSelection);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
}
