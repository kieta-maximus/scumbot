import Discord.InitDiscordMessages;
import GameCommandsInteractions.OpenChatWindow;
import Interfaces.SetThreadActive;
import Listeners.InGameListeners;
import Listeners.NonInGameListeners;
import Parsing.DailyPackJSON;
import Parsing.LottoPackJSON;
import Parsing.WelcomePackJSON;
import RunnablesTimedTasks.online_status_runnable;
import RunnablesTimedTasks.runnable_lottery;
import SQLConnectionManagement.InitDBStructure;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class main {
    public static void main(String[] args) {
        String jsonFileResource = "\\config.json";


        // ... Create file for debug log and set System out to stream to file
        /*try {
            String file_location = new File(main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "/log-" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + ".txt";
            File file = new File(file_location);
            file.createNewFile();
            PrintStream stream = new PrintStream(file);
            System.setOut(stream);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }*/

        BufferedReader reader = null;
        try {
            String json_location = new File(main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + jsonFileResource;
            File f = new File(json_location);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.ISO_8859_1));

        } catch (URISyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
        if (reader == null) {
            throw new NullPointerException("Could not find JSON resource file " + jsonFileResource);
        }

        // Search for any packs to add to database, send to WelcomePackJSON.java for storage and parsing
        try {

            String parent = new File(main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "\\packs_upload"; // Get folder path
            Path dir = Paths.get(parent);
            Files.walk(dir).forEach(path -> {
                File file = path.toFile();
                if (file.isDirectory()) {
                    if (file.getName().equals("welcome_packs")) {
                        try {
                            String welcome_packs_abspath = file.getAbsolutePath();
                            Path wp_dir = Paths.get(welcome_packs_abspath);
                            Files.walk(wp_dir).forEach(wp_path -> {
                                File wp_file = wp_path.toFile();
                                if (wp_file.isFile()) {
                                    // Set buffered file reader and convert contents to JSON object.  Send to welcomepack parser.
                                    try {
                                        BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(wp_file), StandardCharsets.ISO_8859_1));
                                        JSONTokener tokener = new JSONTokener(bReader);
                                        JSONObject obj = new JSONObject(tokener);
                                        new WelcomePackJSON(obj);
                                            bReader.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    boolean deleted = wp_file.delete();
                                    if (!deleted) {
                                        System.out.println("Welcome Pack File was not deleted!");
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (file.getName().equals("lotteries")) {
                        try {
                            String lotto_abspath = file.getAbsolutePath();
                            Path lotto_dir = Paths.get(lotto_abspath);
                            Files.walk(lotto_dir).forEach(lotto_path -> {
                                File lotto_file = lotto_path.toFile();
                                if (lotto_file.isFile()) {
                                    // Set buffered file reader and convert contents to JSON object.  Send to welcomepack parser.
                                    try {
                                        BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(lotto_file), StandardCharsets.ISO_8859_1));
                                        JSONTokener tokener = new JSONTokener(bReader);
                                        JSONObject obj = new JSONObject(tokener);
                                        new LottoPackJSON(obj);
                                        bReader.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    boolean deleted = lotto_file.delete();
                                    if (!deleted) {
                                        System.out.println("Lottery File was not deleted!");
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (file.getName().equals("daily")) {
                        try {
                            String daily_abspath = file.getAbsolutePath();
                            Path daily_dir = Paths.get(daily_abspath);
                            Files.walk(daily_dir).forEach(daily_path -> {
                                File daily_file = daily_path.toFile();
                                if (daily_file.isFile()) {
                                    // Set buffered file reader and convert contents to JSON object.  Send to welcomepack parser.
                                    try {
                                        BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(daily_file), StandardCharsets.ISO_8859_1));
                                        JSONTokener tokener = new JSONTokener(bReader);
                                        JSONObject obj = new JSONObject(tokener);
                                        new DailyPackJSON(obj);
                                        bReader.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    boolean deleted = daily_file.delete();
                                    if (!deleted) {
                                        System.out.println("Daily File was not deleted!");
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (SecurityException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject confJSONObj = new JSONObject(tokener);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String discord_token = confJSONObj.getString("discord_token");
        JDABuilder builder = JDABuilder.createDefault(discord_token);
        try {
            new InitDBStructure();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            SetThreadActive threadWatch = new SetThreadActive();
            builder.addEventListeners(new NonInGameListeners(threadWatch, confJSONObj));
            builder.addEventListeners(new InGameListeners(threadWatch));
            JDA jda = builder.build();
            InitDiscordMessages initMsg = new InitDiscordMessages(confJSONObj, jda);
            new OpenChatWindow();
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            initMsg.GenerateQuickCommands();
            executor.scheduleWithFixedDelay(new online_status_runnable(jda, threadWatch), 0, 30, TimeUnit.SECONDS);
            executor.scheduleWithFixedDelay(new runnable_lottery(jda, threadWatch), 0, 30, TimeUnit.MINUTES);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
