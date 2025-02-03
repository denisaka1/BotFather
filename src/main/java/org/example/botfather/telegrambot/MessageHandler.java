package org.example.botfather.telegrambot;
import org.example.botfather.commands.BotCommand;
import org.example.botfather.commands.BotsManagerBotsCommand;
import org.example.botfather.commands.BotsManagerCreateCommand;
import org.example.botfather.commands.BotsManagerStartCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageHandler {
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<Long, BotCommand> userCommands = new HashMap<>();

    public MessageHandler(BotsManagerStartCommand startCommand, BotsManagerCreateCommand createCommand, BotsManagerBotsCommand botsCommand) {
        commands.put("/start", startCommand);
        commands.put("/create", createCommand);
        commands.put("/bots", botsCommand);
    }

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
        BotCommand currentUserCommand = userCommands.get(userId);
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
            BotCommand command = commands.get(userMessage);
            if (command != null) {
                userCommands.put(userId, command);
                return command.execute(message);
            } else {
                return renderMainMenu(message);
            }
        }
    }
}
