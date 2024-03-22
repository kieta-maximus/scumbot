package Listeners;

import Interfaces.SetThreadActive;
import SQLConnectionManagement.SQLCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.sql.SQLException;

public class NonInGameListeners extends ListenerAdapter {
    SetThreadActive threadStatus;
    JSONObject configJSON;

    public NonInGameListeners(SetThreadActive threadStatus, JSONObject configJSON) {
        this.threadStatus = threadStatus;
        this.configJSON = configJSON;
    }
    @Override
    public void onReady(@NotNull ReadyEvent event){}

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        // ... ignore self and other bots
        if(event.getAuthor().isBot()) {
            return;
        }
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("welcome-pack")) {
            int min = 100000000;
            int max = 999999999;
            int random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
            String welcome_message = String.format("Welcome to the server! BEFORE YOU CONTINUE!!! MAKE SURE YOU ARE IN GAME!!! You can only complete this one time.\r\rTo connect your discord with your in-game character - PLEASE REPLY TO THIS MESSAGE WITH THIS CODE:\r\r ---> !claimwp %s in-game-name-here <--- \r\rReplace your_name_here with your scum name, wait in an open area in the game server for your welcome pack.", random_int);
            event.getInteraction().getMember().getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(welcome_message)).queue();
            event.reply("Welcome to the server!\nPlease check your DM's for more instructions.").setEphemeral(true).queue();
        }
        if (event.getComponentId().equals("add-shop-item")) {
            // Create new modal and text fields
            TextInput itemNameBuilder = TextInput.create("item-name", "Shop Item Name", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMaxLength(255)
                    .setPlaceholder("eg. 1 - 7.62x45 Ammobox")
                    .build();

            TextInput itemPriceBuilder = TextInput.create("item-price", "Shop Item Price", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMaxLength(6)
                    .setPlaceholder("Must be whole number eg: 5000")
                    .build();

            TextInput imageUrlBuilder = TextInput.create("image-url", "Shop Item URL", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Get URL from Imgur with the .png etc in name")
                    .setMaxLength(550)
                    .build();

            TextInput postChannelBuilder = TextInput.create("shop-channel", "Shop Channel ID", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("ID from channel to post")
                    .setMaxLength(20)
                    .build();

            TextInput SCUMAdminContents = TextInput.create("pack-contents", "Contents List from Admin Helper", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setPlaceholder("[{'Content': '#spawnitem xxx'}]")
                    .build();

            Modal modalBuilder = Modal.create("new-shop-item-modal", "Create Shop Item")
                    .addActionRows(
                            ActionRow.of(itemNameBuilder),
                            ActionRow.of(itemPriceBuilder),
                            ActionRow.of(imageUrlBuilder),
                            ActionRow.of(postChannelBuilder),
                            ActionRow.of(SCUMAdminContents))
                    .build();
            event.replyModal(modalBuilder).queue();
        }
        if (event.getComponentId().equals("add-daily")) {
            TextInput SCUMAdminContents = TextInput.create("pack-contents", "Contents List from Admin Helper", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setPlaceholder("[{'Content': '#spawnitem xxx'}]")
                    .build();

            Modal modalBuilder = Modal.create("add-daily-modal", "Create Shop Item")
                    .addActionRows(
                            ActionRow.of(SCUMAdminContents))
                    .build();
            event.replyModal(modalBuilder).queue();
        }
    }
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getInteraction().getModalId().equals("add-shop-item")) {
            // Get List of ModalMappings (values)
            String itemName = event.getInteraction().getValue("item-name").getAsString();
            String itemPrice = event.getInteraction().getValue("item-price").getAsString();
            String imageURL = event.getInteraction().getValue("image-url").getAsString();
            String postChannel = event.getInteraction().getValue("shop-channel").getAsString();
            String SCUMAdminContents = event.getInteraction().getValue("pack-contents").getAsString();
            String commandsChannel = configJSON.getString("commands_channel");

            int min = 10000;
            int max = 99999;
            String random_int = Integer.toString((int) Math.floor(Math.random() * (max - min + 1) + min));

            try {
                new SQLCommand().InsertShopItem(
                        itemName,
                        itemPrice,
                        imageURL,
                        postChannel,
                        SCUMAdminContents,
                        random_int
                );
            } catch (SQLException e) {
                event.reply("There was an error processing. The item was not created.").setEphemeral(true).queue();
                e.printStackTrace();
                return;
            }
            JDA jda = event.getJDA();
            TextChannel postChannelObj = jda.getTextChannelById(postChannel);
            TextChannel commandsChannelObj = jda.getTextChannelById(commandsChannel);
            MessageEmbed embedBuilder = new EmbedBuilder()
                    .setTitle(itemName)
                    .addField("Price", itemPrice, true)
                    .addField("Buy Command", String.format("!buy %s", random_int), true)
                    .addField("Channel", commandsChannelObj.getAsMention(), true)
                    .setColor(Color.GREEN)
                    .setImage(imageURL)
                    .build();
            String buyBtnText = "buy-" + random_int;
            MessageCreateData builder = new MessageCreateBuilder()
                    .addActionRow(
                            Button.primary(buyBtnText, "Buy"))
                    .setEmbeds(embedBuilder)
                    .build();
            if (postChannelObj != null) {
                postChannelObj.sendMessage(builder).queue();
            } else {
                event.reply(String.format("Channel with ID: %s does not exist.", postChannel)).setEphemeral(true).queue();
            }
            event.reply(String.format("%s has been created with pack id %s.", itemName, random_int)).setEphemeral(true).queue();
        } else if (event.getInteraction().getModalId().equals("add-daily-modal")) {
            String contents = event.getInteraction().getValue("pack-contents").getAsString();
            try {
                new SQLCommand().InsertNewDailyPack(contents);
            } catch (SQLException e) {
                event.reply("The was an error adding your daily pack.").setEphemeral(true).queue();
                e.printStackTrace();
            }
            event.reply("Your daily pack has been added").setEphemeral(true).queue();
        }
    }
}

