package RunnablesTimedTasks;

import GameCommandsInteractions.GetPlayerDetails;
import GameCommandsInteractions.OpenChatWindow;
import Interfaces.SetThreadActive;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.json.JSONArray;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class online_status_runnable implements Runnable {
    JDA jda;
    Robot bot;
    SetThreadActive threadWatcher;

    public online_status_runnable(JDA jda, SetThreadActive threadWatcher) {
        this.jda = jda;
        this.threadWatcher = threadWatcher;
    }

    @Override
    public void run() {
        while (threadWatcher.getActiveStatus()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        JSONArray player_details;
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            new OpenChatWindow();
            player_details = new GetPlayerDetails().PlayerDetails();
            bot = new Robot();
            if (clipboard.getData(DataFlavor.stringFlavor).equals("#ListPlayers true")) {
                // ... Assume Bot is not in game, no new text in clipboard.  Bring bot in-game from main menu.  Handle disconnects due to ping or server reboot
                Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();

                double y_coord_double_ok_btn = screen_size.height/ 2.05;
                int y_coord_ok_btn = (int) y_coord_double_ok_btn;
                int x_coord_ok_btn = screen_size.width/2;
                bot.mouseMove(x_coord_ok_btn, y_coord_ok_btn);
                bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                try {

                    Thread.sleep(100);

                    bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                    Thread.sleep(3000);

                    double x_coord_double_continue_btn = screen_size.getWidth() * 0.18;
                    double y_coord_double_continue_btn = screen_size.getHeight() * 0.59;
                    int y_coord_continue_btn = (int) y_coord_double_continue_btn;
                    int x_coord_continue_btn = (int) x_coord_double_continue_btn;
                    bot.mouseMove(x_coord_continue_btn, y_coord_continue_btn);

                    bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

                    Thread.sleep(100);

                    bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                    Thread.sleep(30000); // ... Sleep long enough for client to load, potato machine alert

                    new OpenChatWindow();
                    bot.keyPress(KeyEvent.VK_TAB);
                    bot.keyRelease(KeyEvent.VK_TAB);
                    int player_details_length = new GetPlayerDetails().PlayerDetails().length();
                    if (player_details_length >= 1) {
                        jda.getPresence().setActivity(Activity.playing(String.format("SCUM with %s others | !help", player_details_length)));
                    } else {
                        jda.getPresence().setActivity(Activity.playing("IGC is down | !help"));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                int players_online = player_details.length()-1;
                jda.getPresence().setActivity(Activity.playing(String.format("SCUM with %s others | !help", players_online)));
                new OpenChatWindow();
                // new GetPlayerDetails().SetTPLocation();
                // ... TODO add squad tracking
                // ... Update Squad members and squad names in DB
            }
        } catch (AWTException | UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
    }
}
