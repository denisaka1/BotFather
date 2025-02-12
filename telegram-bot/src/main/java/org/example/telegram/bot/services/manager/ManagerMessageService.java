package org.example.telegram.bot.services.manager;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.bot.actions.manager.*;
import org.example.telegram.bot.services.dynamic.RegistrationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@AllArgsConstructor
public class ManagerMessageService {
    private final StartSlashCommand startSlashCommand;
    private final CreateSlashCommand createSlashCommand;
    private final BotsSlashCommand botsSlashCommand;

    public String processMessage(Message message) {
        return renderSlashCommand(message);
    }

    private String renderSlashCommand(Message message) {
        String userMessage = message.getText().toLowerCase();
        switch (userMessage) {
            case "/cancel" -> {
                return "❌ Command cancelled!" + "\n\n" + renderMainMenu(message);
            }
            case "/start" -> {
                return startSlashCommand.execute(message);
            }
            case "/create" -> {
                return createSlashCommand.execute(message);
            }
            case "/bots" -> {
                return botsSlashCommand.execute(message);
            }
            default -> {
                return "❌ Command not supported!\n\n" + renderMainMenu(message);
            }
        }
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
