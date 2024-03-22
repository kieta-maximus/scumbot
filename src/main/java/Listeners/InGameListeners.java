package Listeners;

import GameCommandsInteractions.DeliverInGamePackage;
import GameCommandsInteractions.GetPlayerDetails;
import GameCommandsInteractions.OpenChatWindow;
import Interfaces.SetThreadActive;
import SQLConnectionManagement.SQLCommand;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

public class InGameListeners extends ListenerAdapter {
    String jsonFileResource = "\\config.json";
    BufferedReader reader = null;
    JSONTokener parser;
    JSONObject server_config_json;
    SetThreadActive threadStatus;

    public InGameListeners(SetThreadActive threadStatus) {
        this.threadStatus = threadStatus;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            String json_location = new File(InGameListeners.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + jsonFileResource;
            File f = new File(json_location);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.ISO_8859_1));
        } catch (URISyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
        if (reader == null) {
            throw new NullPointerException("Could not find JSON resource file " + jsonFileResource);
        }
        parser = new JSONTokener(reader);
        server_config_json = new JSONObject(parser);

        // ... Perform lottery every 40 minutes

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        JSONArray online_players;
        JSONObject database_player;

        // ... ignore self and other bots
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getMessage().getContentDisplay().contains("!claimwp")) {
            String[] message_list = event.getMessage().getContentDisplay().split(" ");
            String scum_name = "";
            String welcome_pack = "";

            for (int i=0; i<message_list.length; i++) {
                if (i==1) {
                    welcome_pack = message_list[i];
                } else if (i==2) {
                    scum_name = message_list[i];
                } else {
                    scum_name = scum_name.concat(String.format("%s", message_list[i])).toLowerCase(Locale.ROOT);
                }
            }
            scum_name = scum_name.toLowerCase(Locale.ROOT);

            try {
                new OpenChatWindow();
                online_players = new GetPlayerDetails().PlayerDetails();
                for (int i = 0; i < online_players.length(); i++) {
                    JSONObject online_json = online_players.getJSONObject(i);
                    String steam_id = online_json.getString("steam_id");
                    String discord_id = event.getAuthor().getId();
                    if (online_json.getString("scum_name").toLowerCase().equals(scum_name)) {
                        database_player = new SQLCommand().SelectSinglePlayerDb("steam_id", steam_id);
                        if (database_player.length() == 0) {
                            boolean insert_player = new SQLCommand().InsertSinglePlayerDb(scum_name, discord_id, welcome_pack, steam_id);
                            if (insert_player) {
                                String welcome_message = String.format("<@%s> Welcome to the server! We have received your welcome pack request successfully.\r\rPlease remain in an open area in game, your welcome pack is on its way!", discord_id);
                                String welcome_channel = server_config_json.getString("welcome_pack_channel");
                                TextChannel channel = event.getJDA().getTextChannelById(welcome_channel);
                                if (channel != null) {
                                    channel.sendMessage(welcome_message).queue();

                                    String random_welcome_pack = new SQLCommand().SelectRandomWelcomePack();
                                    new DeliverInGamePackage(random_welcome_pack, steam_id, threadStatus);
                                } else {
                                    System.out.println("Welcome Channel not found or defined, sending as a dm.");
                                    event.getAuthor().openPrivateChannel().queue((private_channel) ->
                                            private_channel.sendMessage(welcome_message).queue());
                                }
                                return;
                            } else {
                                String error_message = "We had an error in our database, perhaps the entry already exists.  Please open a support ticket in Discord.";
                                event.getAuthor().openPrivateChannel().queue((private_channel) ->
                                        private_channel.sendMessage(error_message).queue());
                                return;
                            }
                        } else {
                            String error_message = "It looks like you have already claimed your welcome pack.  Please open a support ticket in Discord if this is not true.";
                            event.getAuthor().openPrivateChannel().queue((private_channel) ->
                                    private_channel.sendMessage(error_message).queue());
                            return;
                        }
                    }
                }
                String error_message = "We couldn't find you in game.  Your character name is case-sensitive.  You can try again if you noticed an error.";
                event.getAuthor().openPrivateChannel().queue((private_channel) ->
                        private_channel.sendMessage(error_message).queue());
            } catch (AWTException | SQLException e) {
                e.printStackTrace();
            }
        }

        if (event.getMessage().getContentDisplay().contains("!claimlotto")) {
            String[] message_list = event.getMessage().getContentDisplay().split(" ");
            String lotto_id = message_list[1];
            String discord_id = event.getAuthor().getId();

            try {
                String steam_id = new SQLCommand().SelectLotteryClaim(lotto_id, discord_id);
                JSONObject random_lotto_json = new SQLCommand().SelectRandomLottoPack();

                if (steam_id != null) {
                    String success_message = "Your lotto pack is incoming! Check lotto results channel for a sneak peak at your lotto incoming!";
                    event.getAuthor().openPrivateChannel().queue((private_channel) ->
                            private_channel.sendMessage(success_message).queue());

                    String lotto_results_message = String.format("<@%s> has claimed their lottery! They will be receiving the pack: %s\r\rPlease remain in an open area in game, your lottery winnings are on the way!", discord_id, random_lotto_json.getString("pack_name"));
                    String lotto_channel = server_config_json.getString("lotto_results_channel");
                    TextChannel channel = event.getJDA().getTextChannelById(lotto_channel);
                    if (channel != null) {
                        channel.sendMessage(lotto_results_message).queue();
                    } else {
                        System.out.println("Lotto Channel not found or defined, sending as a dm.");
                        event.getAuthor().openPrivateChannel().queue((private_channel) ->
                                private_channel.sendMessage(lotto_results_message).queue());
                    }
                    new DeliverInGamePackage(random_lotto_json.getString("game_command"), steam_id, threadStatus);
                    new SQLCommand().UpdateClaimedLotto(discord_id, steam_id);
                } else {
                    String error_message = "We couldn't find a lotto pack with that id.  Try again or open a support ticket in Discord.";
                    event.getAuthor().openPrivateChannel().queue((private_channel) ->
                            private_channel.sendMessage(error_message).queue());
                }
            } catch (SQLException | AWTException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("daily")) {
            JSONObject player_data = new JSONObject();
            JSONObject pack_data = new JSONObject();
            String server_name = server_config_json.getString("server_name");
            String daily_result = String.format("Your daily pack is incoming on %s. Stay safe out there.", server_name);
            event.reply(daily_result).setEphemeral(true).queue();
            String discordID = event.getInteraction().getMember().getUser().getId();
            try {
                player_data = new SQLCommand().SelectSinglePlayerDb("discord_id", discordID);
                pack_data = new SQLCommand().SelectRandomDailyPack();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (player_data.isEmpty() || pack_data.isEmpty()) {
                try {
                    new DeliverInGamePackage(pack_data.getString("game_command"), player_data.getString("steam_id"), threadStatus);
                    new SQLCommand().UpdateDailyTime(player_data.getString("steam_id"), server_config_json.getInt("daily_coin_amount"));
                } catch (AWTException | SQLException e) {
                    e.printStackTrace();
                }
            } else {
                if (player_data.isEmpty()) { System.out.println("Player data is empty"); }
                if (pack_data.isEmpty()) { System.out.println("Pack data is empty"); }
            }
        }
        if (event.getComponentId().contains("buy")) {
            String[] message_list = event.getComponentId().split("-");
            String item_id = message_list[1];
            String discordID = event.getInteraction().getMember().getId();

            JSONObject itemDetails = new JSONObject();
            JSONObject playerDetails = new JSONObject();

            int player_balance = 0;
            String player_steam_id = "";
            try {
                playerDetails = new SQLCommand().CheckPlayerBalance(discordID);
                itemDetails = new SQLCommand().SelectShopItem(item_id);
                player_balance = playerDetails.getInt("player_balance");
                player_steam_id = playerDetails.getString("steam_id");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (!itemDetails.isEmpty() && player_balance > itemDetails.getInt("price")) {
                event.reply("Your pack has been ordered! Please wait in an open area in-game.").setEphemeral(true).queue();
                try {
                    new DeliverInGamePackage(itemDetails.getString("commands"), player_steam_id, threadStatus);
                    int newBalance = player_balance - itemDetails.getInt("price");
                    new SQLCommand().UpdatePlayerBalance(newBalance, discordID);
                } catch (AWTException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
