package org.example.telegram.bot.services.manager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.telegram.bot.actions.manager.BotsSlashCommand;
import org.example.telegram.bot.actions.manager.CreateSlashCommand;
import org.example.telegram.bot.actions.manager.SlashCommand;
import org.example.telegram.bot.actions.manager.StartSlashCommand;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

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
    private Map<String, Boolean> commands;
    private Long userId;

    public SendMessage processMessage(Message message) {
        userMessage = message.getText().toLowerCase();
        userId = message.getFrom().getId();
        return renderSlashCommand(message);
    }

    @PostConstruct
    public void init() {
        commands = new HashMap<>();
        commands.put(SlashCommand.CANCEL, Boolean.FALSE);
        commands.put(SlashCommand.START, Boolean.FALSE);
        commands.put(SlashCommand.CREATE, Boolean.FALSE);
        commands.put(SlashCommand.BOTS, Boolean.FALSE);
    }

    private SendMessage renderSlashCommand(Message message) {
        switch (userMessage) {
            case SlashCommand.CANCEL -> {
                commands.replace(SlashCommand.START, Boolean.FALSE);
                commands.replace(SlashCommand.CREATE, Boolean.FALSE);
                commands.replace(SlashCommand.BOTS, Boolean.FALSE);
                return createSimpleMessage("❌ Command cancelled!" + "\n\n" + renderMainMenu(message));
            }
            case SlashCommand.START -> {
                if (isOwnerRegistered()) {
                    return createSimpleMessage("👋 Welcome back! You are already registered!\n Type any text to continue.");
                }
                commands.replace(SlashCommand.START, Boolean.TRUE);
                return createSimpleMessage(SlashCommand.BACK_COMMAND_MESSAGE + startSlashCommand.execute(message));
            }
            case SlashCommand.CREATE -> {
                if (isOwnerRegistered()) {
                    commands.replace(SlashCommand.CREATE, Boolean.TRUE);
                    return createSimpleMessage(SlashCommand.BACK_COMMAND_MESSAGE + createSlashCommand.execute(message));
                }

                return createSimpleMessage("""
                        👋 Welcome to the Bots Creator!
                        You need to register using the /start command to create a new bot.
                        Type any text to return to the menu.""");
            }
            case SlashCommand.BOTS -> {
                return createSimpleMessage(botsSlashCommand.execute(message));
            }
            default -> {
                return handleUserResponse(message);
            }
        }
    }

    private SendMessage createSimpleMessage(String message) {
        return MessageGenerator.createSimpleTextMessage(userId, message);
    }

    private SendMessage handleUserResponse(Message message) {
        if (!isSlashCommandStarted()) {
            return createSimpleMessage(renderMainMenu(message));
        }

        if (Objects.equals(startedCommand(), SlashCommand.CREATE)) {
            return createSimpleMessage(processCreateCommand(message));
        } else if (Objects.equals(startedCommand(), SlashCommand.START)) {
            return createSimpleMessage(processStartCommand(message));
        } else { // /cancel
            return createSimpleMessage(renderMainMenu(message));
        }
    }

    private String processCreateCommand(Message message) {
        if (!createSlashCommand.isCompleted()) {
            return createSlashCommand.processUserResponse(message);
        }

        commands.replace(SlashCommand.CREATE, Boolean.FALSE);
        return renderMainMenu(message);
    }

    private String processStartCommand(Message message) {
        if (!startSlashCommand.isCompleted()) {
            return startSlashCommand.processUserResponse(message);
        }

        commands.replace(SlashCommand.START, Boolean.FALSE);
        return renderMainMenu(message);
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

    private String renderMainMenu(Message message) {
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

}
