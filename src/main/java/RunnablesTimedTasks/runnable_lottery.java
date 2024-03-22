package RunnablesTimedTasks;

import GameCommandsInteractions.GetOnlinePlayersLotto;
import GameCommandsInteractions.OpenChatWindow;
import GameCommandsInteractions.SendMessageInGame;
import Interfaces.SetThreadActive;
import SQLConnectionManagement.SQLCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.json.JSONObject;

import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class runnable_lottery implements Runnable {
    JDA jda;
    SetThreadActive threadWatcher;

    public runnable_lottery (JDA jda, SetThreadActive threadWatcher) {
        this.jda = jda;
        this.threadWatcher = threadWatcher;
    }
    @Override
    public void run() {
        // ... Instantiate parameters
        while (threadWatcher.getActiveStatus()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("------- Running Lottery: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + "-------");
        List<String> player_details;
        JSONObject singlePlayerJson;
        String steam_id;
        int random_lotto_num;

        try {
            // ... Instantiate random number for winner and prepare/send message to community
            new OpenChatWindow();
            int min = 100000000;
            int max = 999999999;
            player_details = new GetOnlinePlayersLotto().PlayerDetails();
            if (player_details.size() > 1) {
                String prepare_message = String.format("The lottery will be running in 1-minute, there are %s players online.  If you haven't registered in the discord, do so now: https://discord.gg/rbHqJ9mmGb", player_details.size()-1);
                new SendMessageInGame(prepare_message); // ... Send a message in-game indicating the lottery will begin soon.  Allow players times to register before running.
                try { // ... Sleep the thread for 60 second to allow players opportunity to register on Discord.
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                singlePlayerJson = new SQLCommand().SelectRandomLottoWinner(player_details);

                random_lotto_num = (int)Math.floor(Math.random()*(max-min+1)+min);

                steam_id = singlePlayerJson.getString("steam_id");
                new OpenChatWindow();
                String winner_announcement_game = String.format("%s is the winner of this lottery. Check your Discord DM's for details.", singlePlayerJson.getString("scum_name"));
                new SendMessageInGame(winner_announcement_game); // ... Send lottery results to game server community
                new SQLCommand().InsertLotteryResults(singlePlayerJson.getString("discord_id"), steam_id, random_lotto_num);
                String message = String.format("Congrats! You won the lottery on Nass Scum Server! \r\r!claimlotto %s\r\rReply with the message above to claim your lottery.", random_lotto_num);

                User jdaUser = jda.retrieveUserById(singlePlayerJson.getString("discord_id")).complete();
                jdaUser.openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage(message))
                        .queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, (ex)
                                -> System.out.println("Could not send message")));
            } else {
                String prepare_message = "There were not enough players for the lotto to run.  Join discord to register (!welcomepack) and catch the next one: https://discord.gg/rbHqJ9mmGb";
                new SendMessageInGame(prepare_message); // ... Send a message in-game indicating the lottery will begin soon.  Allow players times to register before running.
            }
        } catch (AWTException | SQLException e) {
            e.printStackTrace();
            System.out.println("Failed");
        }
    }
}
