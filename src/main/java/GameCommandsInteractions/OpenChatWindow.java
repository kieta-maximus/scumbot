package GameCommandsInteractions;

import java.awt.*;
import java.awt.event.KeyEvent;

public class OpenChatWindow {

    Robot robot;
    public OpenChatWindow() throws AWTException {

        robot = new Robot();
        try {
            robot.keyPress(KeyEvent.VK_T);
            robot.keyRelease(KeyEvent.VK_T);
            Thread.sleep(500);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyRelease(KeyEvent.VK_A);
            Thread.sleep(500);
            robot.keyPress(KeyEvent.VK_BACK_SPACE);
            robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
