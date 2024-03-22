package Discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

public class InitDiscordMessages {
    JSONObject serverConfJSON;
    JDA jda;
    TextChannel quickCommChannelObj;
    TextChannel adminCommChannelObj;

    public InitDiscordMessages(JSONObject serverConfJSON, JDA jda) {
        this.serverConfJSON = serverConfJSON;
        this.jda=jda;
    }

    public void GenerateQuickCommands() {
        String quickCommChannelID = serverConfJSON.getString("quick_commands_channel");
        String adminCommChannelID = serverConfJSON.getString("admin_channel");
        String quickCommMessageID = serverConfJSON.getString("quick_commands_message");
        String adminCommMessageID = serverConfJSON.getString("admin_commands_message");

        adminCommChannelObj = jda.getTextChannelById(adminCommChannelID);
        quickCommChannelObj = jda.getTextChannelById(quickCommChannelID);
        if (quickCommMessageID.isBlank()) {
            quickCommMessageID = "1";
        }
        if (adminCommMessageID.isBlank()) {
            adminCommMessageID = "1";
        }
        if (adminCommChannelObj != null) {
            adminCommChannelObj.retrieveMessageById(adminCommMessageID).queue((message) -> {

            }, (failure) -> {
                AddNewAdminCommands();
            });
        }
        if (quickCommChannelObj != null) {
            quickCommChannelObj.retrieveMessageById(quickCommMessageID).queue((message) -> {

            }, (failure) -> {
                AddNewQuickCommandsMessage();
            });
        } else {
            System.out.println("Quick Commands Channel is null!");
        }
    }
    private void AddNewQuickCommandsMessage() {
        MessageEmbed embedBuilder = new EmbedBuilder()
                .setTitle("Select an option below")
                .setDescription("Run commands quickly with the buttons below.")
                .setColor(Color.yellow)
                .build();

        MessageCreateData builder = new MessageCreateBuilder()
                .addActionRow(
                        Button.primary("welcome-pack", "Get Welcome Pack"),
                        Button.primary("daily", "Claim Daily Gift"))
                .setEmbeds(embedBuilder)
                .build();

        try {
            quickCommChannelObj.sendMessage(builder).queue((message) -> {
                serverConfJSON.put("quick_commands_message", message.getId());

                try {
                    String json_location = new File(InitDiscordMessages.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "\\config.json";
                    PrintWriter writer = new PrintWriter(new FileWriter(json_location));
                    writer.write(serverConfJSON.toString());
                    writer.close();
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            });
        } catch (InsufficientPermissionException e) {
            System.out.println("Discord Bot does not have correct permissions to write a message to this channel.");
            e.printStackTrace();
        }
    }
    private void AddNewAdminCommands() {
        MessageEmbed embedBuilder = new EmbedBuilder()
                .setTitle("Select an option below")
                .setDescription("Run commands quickly with the buttons below.")
                .setColor(Color.yellow)
                .build();

        MessageCreateData builder = new MessageCreateBuilder()
                .addActionRow(
                        Button.primary("add-shop-item", "Add Shop Item"),
                        Button.primary("add-daily", "Add Daily Pack"))
                .setEmbeds(embedBuilder)
                .build();

        try {
            adminCommChannelObj.sendMessage(builder).queue((message) -> {
                serverConfJSON.put("admin_commands_message", message.getId());

                try {
                    String json_location = new File(InitDiscordMessages.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "\\config.json";
                    PrintWriter writer = new PrintWriter(new FileWriter(json_location));
                    writer.write(serverConfJSON.toString());
                    writer.close();
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            });
        } catch (InsufficientPermissionException e) {
            System.out.println("Discord Bot does not have correct permissions to write a message to this channel.");
            e.printStackTrace();
        }
    }
}
