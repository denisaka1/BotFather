package org.example.bots.manager.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.bots.manager.actions.BotsSlashCommand;
import org.example.bots.manager.actions.CreateSlashCommand;
import org.example.bots.manager.actions.SlashCommand;
import org.example.bots.manager.actions.StartSlashCommand;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ManagerMessageService {

    private final StartSlashCommand startSlashCommand;
    private final CreateSlashCommand createSlashCommand;
    private final BotsSlashCommand botsSlashCommand;
    private final BusinessOwnerApi businessOwnerApi;

    private String userMessage;
    private Update update;
    private Message message;
    private Map<String, Boolean> commands;
    private Long userId;

    public BotApiMethod<?> processTextMessage(Update update) {
        this.update = update;
        message = update.getMessage();
        userMessage = message.getText().toLowerCase();
        userId = message.getFrom().getId();
        return renderSlashCommand();
    }

    public BotApiMethod<?> processCallbackCommand(Update update) {
        if (isAppointmentConfirmation(update)) {
            //            handleAppointmentMessage(update); // adjust message to correct state
            // edit the markup correctly
//            SendMessage response = SendMessage.builder()
//                    .chatId(update.getCallbackQuery().getMessage().getChatId())
//                    .text("Confirmed");
//            dynamicBot.handleAppointmentResponse()
//            TODO: finish it
            return null;
        }
        return botsSlashCommand.processCallbackResponse(update);
    }

    @PostConstruct
    private void init() {
        commands = new HashMap<>();
        commands.put(SlashCommand.CANCEL, Boolean.FALSE);
        commands.put(SlashCommand.START, Boolean.FALSE);
        commands.put(SlashCommand.CREATE, Boolean.FALSE);
    }

    private BotApiMethod<?> renderSlashCommand() {
        switch (userMessage) {
            case SlashCommand.CANCEL -> {
                commands.replace(SlashCommand.START, Boolean.FALSE);
                commands.replace(SlashCommand.CREATE, Boolean.FALSE);
                commands.replace(SlashCommand.BOTS, Boolean.FALSE);
                return createSimpleMessage("❌ Command cancelled!" + "\n\n" + renderMainMenu());
            }
            case SlashCommand.START -> {
                if (isOwnerRegistered()) {
                    return createSimpleMessage("👋 Welcome back! You are already registered!\n Type any text to continue.");
                }
                commands.replace(SlashCommand.START, Boolean.TRUE);
                return createSimpleMessage(SlashCommand.BACK_COMMAND_MESSAGE + startSlashCommand.execute(message).getText());
            }
            case SlashCommand.CREATE -> {
                if (isOwnerRegistered()) {
                    commands.replace(SlashCommand.CREATE, Boolean.TRUE);
                    return createSimpleMessage(SlashCommand.BACK_COMMAND_MESSAGE + createSlashCommand.execute(message).getText());
                }

                return createSimpleMessage("""
                        👋 Welcome to the Bots Creator!
                        You need to register using the /start command to create a new bot.
                        Type any text to return to the menu.""");
            }
            case SlashCommand.BOTS -> {
                return botsSlashCommand.execute(message);
            }
            default -> {
                return handleUserResponse();
            }
        }
    }

    private BotApiMethod<?> handleUserResponse() {
        if (!isSlashCommandStarted()) {
            return createSimpleMessage(renderMainMenu());
        }

        if (Objects.equals(startedCommand(), SlashCommand.CREATE)) {
            return createSimpleMessage(processCreateCommand());
        } else if (Objects.equals(startedCommand(), SlashCommand.START)) {
            return createSimpleMessage(processStartCommand());
        } else { // /cancel
            return createSimpleMessage(renderMainMenu());
        }
    }

    private String processCreateCommand() {
        if (!createSlashCommand.isCompleted()) {
            return createSlashCommand.processUserResponse(message);
        }

        commands.replace(SlashCommand.CREATE, Boolean.FALSE);
        return renderMainMenu();
    }

    private String processStartCommand() {
        if (!startSlashCommand.isCompleted()) {
            return startSlashCommand.processUserResponse(message);
        }

        commands.replace(SlashCommand.START, Boolean.FALSE);
        return renderMainMenu();
    }

    private boolean isSlashCommandStarted() {
        for (Boolean commandValue : commands.values()) {
            if (Boolean.TRUE.equals(commandValue)) {
                return true;
            }
        }
        return false;
    }

    private String startedCommand() {
        for (Map.Entry<String, Boolean> command : commands.entrySet()) {
            if (Boolean.TRUE.equals(command.getValue())) {
                return command.getKey();
            }
        }
        return SlashCommand.CANCEL;
    }

    public boolean isOwnerRegistered() {
        return businessOwnerApi.isRegistered(userId);
    }

    private String renderMainMenu() {
        String userFirstName = message.getFrom().getFirstName();
        return String.format("""
                Hello %s! 👋
                🌟 Welcome to Our Bot! 🌟
                
                🏠 Main Menu:
                
                🔹 /start - 🚀 Create a new user
                🔹 /create - 🤖 Create a new bot
                🔹 /bots - 📋 List all your bots
                
                ℹ️ You can return to this menu anytime by typing /cancel. ❌
                """, userFirstName);
    }

    private SendMessage createSimpleMessage(String message) {
        return MessageGenerator.createSimpleTextMessage(userId, message);
    }

    private boolean isAppointmentConfirmation(Update update) {
        return update.getCallbackQuery().getData().startsWith("confirmAppointment") ||
                update.getCallbackQuery().getData().startsWith("declineAppointment");
    }
}
