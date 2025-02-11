package org.example.telegram.bot.services.manager;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.bot.actions.manager.BotsManagerBotsCommand;
import org.example.telegram.bot.actions.manager.BotsManagerCreateCommand;
import org.example.telegram.bot.actions.manager.BotsManagerStartCommand;
import org.example.telegram.bot.actions.manager.IBotCommand;
import org.example.telegram.bot.services.dynamic.RegistrationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class ManagerMessageService {
    private final ApiRequestHelper apiRequestHelper;
    private final RegistrationService botsRegistryService;
    private final Map<Long, IBotCommand> userCommands = new HashMap<>();

    public String renderMainMenu(Message message) {
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

    public String processMessage(Message message) {
        String userMessage = message.getText().toLowerCase();
        Long userId = message.getFrom().getId();
        IBotCommand currentUserCommand = userCommands.get(userId);
        if (currentUserCommand != null) {
            if (userMessage.equals("/cancel")) {
                userCommands.remove(userId);
                return "❌ Command cancelled!" + "\n\n" + renderMainMenu(message);
            }
            if (currentUserCommand.isCompleted()) {
                userCommands.remove(userId);
                return renderMainMenu(message);
            }
            return currentUserCommand.execute(message);
        } else {
            IBotCommand command = getCommand(userMessage);
            if (command != null) {
                userCommands.put(userId, command);
                return command.execute(message);
            } else {
                return renderMainMenu(message);
            }
        }
    }

    public IBotCommand getCommand(String command) {
        if (command == null) return null;

        return switch (command) {
            case "/start" -> new BotsManagerStartCommand(apiRequestHelper);
            case "/create" -> new BotsManagerCreateCommand(apiRequestHelper, botsRegistryService);
            case "/bots" -> new BotsManagerBotsCommand(apiRequestHelper);
            default -> null;
        };
    }
}
