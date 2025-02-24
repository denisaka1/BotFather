package org.example.bots.manager.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.bots.manager.actions.slash.*;
import org.example.bots.manager.constants.Callback;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Service;
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
    private final ScheduleSlashCommand scheduleSlashCommand;
    private final BusinessOwnerApi businessOwnerApi;

    private final MessageBatchProcessor messageBatchProcessor;
    private final BotApi botApi;

    private String userMessage;
    private Message message;
    private Map<String, Boolean> commands;
    private Long userId;

    public void processTextMessage(Update update) {
        message = update.getMessage();
        userMessage = message.getText().toLowerCase();
        userId = message.getFrom().getId();
        renderSlashCommand();
    }

    public void processCallbackCommand(Update update) {
        if (isEditBotCallback(update)) {
            commands.replace(SlashCommand.BOTS, Boolean.TRUE);
        }

        if (isScheduleCallback(update)) {
            commands.replace(SlashCommand.SCHEDULE, Boolean.TRUE);
        }

        if (isAppointmentConfirmation(update)) {
            // TODO: finish appointment handling
//            handleAppointmentMessage(update); // adjust message to correct state
            // edit the markup correctly
//            SendMessage response = SendMessage.builder()
//                    .chatId(update.getCallbackQuery().getMessage().getChatId())
//                    .text("Confirmed");
//            dynamicBot.handleAppointmentResponse()
        } else {
            if (commands.get(SlashCommand.BOTS)) {
                botsSlashCommand.processCallbackResponse(update);
            } else if (commands.get(SlashCommand.SCHEDULE)) {
                scheduleSlashCommand.processCallbackResponse(update);
            }
        }
    }

    private boolean isEditBotCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        return callbackData.startsWith(Callback.EDIT_BOT_NAME) ||
                callbackData.startsWith(Callback.EDIT_BOT_WORKING_HOURS) ||
                callbackData.startsWith(Callback.EDIT_BOT_TOKEN) ||
                callbackData.startsWith(Callback.EDIT_BOT_WELCOME_MESSAGE) ||
                callbackData.startsWith(Callback.EDIT_BOT_JOBS);
    }

    private boolean isScheduleCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        return Callback.SCHEDULE_CALLBACKS.stream().anyMatch(callbackData::startsWith);
    }

    @PostConstruct
    private void init() {
        commands = new HashMap<>();
        commands.put(SlashCommand.CANCEL, Boolean.FALSE);
        commands.put(SlashCommand.START, Boolean.FALSE);
        commands.put(SlashCommand.CREATE, Boolean.FALSE);
        commands.put(SlashCommand.BOTS, Boolean.FALSE);
        commands.put(SlashCommand.SCHEDULE, Boolean.FALSE);
    }

    private void renderSlashCommand() {
        switch (userMessage) {
            case SlashCommand.CANCEL -> {
                cancelPreviousSlashCommand();
                commands.replace(SlashCommand.START, Boolean.FALSE);
                commands.replace(SlashCommand.CREATE, Boolean.FALSE);
                commands.replace(SlashCommand.BOTS, Boolean.FALSE);
                addSimpleMessage("❌ Command cancelled!" + "\n\n" + renderMainMenu());
            }
            case SlashCommand.START -> {
                if (isOwnerRegistered()) {
                    addSimpleMessage("👋 Welcome back! You are already registered!\n Type any text to continue.");
                    return;
                }
                commands.replace(SlashCommand.START, Boolean.TRUE);
                startSlashCommand.execute(message);
            }
            case SlashCommand.CREATE -> {
                if (isOwnerRegistered()) {
                    commands.replace(SlashCommand.CREATE, Boolean.TRUE);
                    createSlashCommand.execute(message);
                    return;
                }

                addSimpleMessage("""
                        👋 Welcome to the Bots Creator!
                        You need to register using the /start command to create a new bot.
                        Type any text to return to the menu.""");
            }
            case SlashCommand.BOTS -> botsSlashCommand.execute(message);
            case SlashCommand.SCHEDULE -> scheduleSlashCommand.execute(message);
            default -> handleUserResponse();
        }
    }

    private void cancelPreviousSlashCommand() {
        if (Objects.equals(startedCommand(), SlashCommand.BOTS)) {
            Bot bot = businessOwnerApi.getEditableBot(userId);
            bot.setCreationState(BotCreationState.COMPLETED);
            botApi.updateBot(bot);
        }
    }

    private void handleUserResponse() {
        if (!isSlashCommandStarted()) {
            addSimpleMessage(renderMainMenu());
            return;
        }

        if (Objects.equals(startedCommand(), SlashCommand.CREATE)) {
            addSimpleMessage(processCreateCommand());
        } else if (Objects.equals(startedCommand(), SlashCommand.START)) {
            addSimpleMessage(processStartCommand());
        } else if (Objects.equals(startedCommand(), SlashCommand.BOTS)) {
            processBotsCommand();
        } else { // /cancel
            addSimpleMessage(renderMainMenu());
        }
    }

    private String processCreateCommand() {
        if (!createSlashCommand.isCompleted(userId)) {
            return createSlashCommand.processUserResponse(message);
        }

        commands.replace(SlashCommand.CREATE, Boolean.FALSE);
        return renderMainMenu();
    }

    private void processBotsCommand() {
        commands.replace(SlashCommand.BOTS, Boolean.FALSE);
        botsSlashCommand.processUserResponse(message);
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
                🔹 /schedule - 📅 Manage your appointments
                
                ℹ️ You can return to this menu anytime by typing /cancel. ❌
                """, userFirstName);
    }

    private void addSimpleMessage(String message) {
        messageBatchProcessor.addMessage(
                MessageGenerator.createSimpleTextMessage(userId, message)
        );
    }

    private boolean isAppointmentConfirmation(Update update) {
        return update.getCallbackQuery().getData().startsWith("confirmAppointment") ||
                update.getCallbackQuery().getData().startsWith("declineAppointment");
    }
}
