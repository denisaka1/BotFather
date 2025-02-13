package org.example.telegram.bot.services.manager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.telegram.bot.actions.manager.*;
import org.springframework.stereotype.Service;
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

    private String userMessage;
    private Map<String, Boolean> commands;
    private Long chatId;

    public String processMessage(Message message) {
        userMessage = message.getText().toLowerCase();
        chatId = message.getChatId();
        return renderSlashCommand(message);
    }

    @PostConstruct
    public void init() {
        commands = new HashMap<>();
        commands.put("/cancel", Boolean.FALSE);
        commands.put("/start", Boolean.FALSE);
        commands.put("/create", Boolean.FALSE);
        commands.put("/bots", Boolean.FALSE);
    }

    private String renderSlashCommand(Message message) {
        switch (userMessage) {
            case "/cancel" -> {
                commands.replace("/start", Boolean.FALSE);
                commands.replace("/create", Boolean.FALSE);
                commands.replace("/bots", Boolean.FALSE);
                return "❌ Command cancelled!" + "\n\n" + renderMainMenu(message);
            }
            case "/start" -> {
                commands.replace("/start", Boolean.TRUE);
                return startSlashCommand.execute(message);
            }
            case "/create" -> {
                commands.replace("/create", Boolean.TRUE);
                return createSlashCommand.execute(message);
            }
            case "/bots" -> {
                commands.replace("/bots", Boolean.TRUE);
                return botsSlashCommand.execute(message);
            }
            default -> {
                return handleUserResponse(message);
            }
        }
    }

    private String handleUserResponse(Message message) {
        if (!isSlashCommandStarted()) {
            return renderMainMenu(message);
        }

        if (Objects.equals(startedCommand(), "/create")) {
            if (createSlashCommand.isCompleted()) {
                commands.replace("/create", Boolean.FALSE);
                return renderMainMenu(message);
            } else {
                return createSlashCommand.processUserResponse(message);
            }
        } else if (Objects.equals(startedCommand(), "/start")) {
//            return startSlashCommand.processUserResponse(message);
        } else if (Objects.equals(startedCommand(), "/bots")) {
            return botsSlashCommand.execute(message);
        } else { // /cancel
            return renderMainMenu(message);
        }
//        return "";
        return "";
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
        return "/cancel";
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

//    public ISlashCommand getCommand(String command) {
//        if (command == null) return null;
//
//        return switch (command) {
//            case "/start" -> new StartSlashCommand(apiRequestHelper);
//            case "/create" -> new CreateSlashCommand(apiRequestHelper, botsRegistryService);
//            case "/bots" -> new BotsSlashCommand(apiRequestHelper);
//            default -> null;
//        };
//    }
}
