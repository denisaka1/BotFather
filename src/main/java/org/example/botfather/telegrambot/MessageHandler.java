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

    public String processMessage(Message message) {
        String userMessage = message.getText().toLowerCase();
        Long userId = message.getFrom().getId();
        if (userCommands.containsKey(userId)) {
            if (userMessage.equals("/cancel")) {
                userCommands.remove(userId);
                return "Command cancelled";
            }
            return userCommands.get(userId).execute(message);
        } else {
            BotCommand command = commands.get(userMessage);
            if (command != null) {
                userCommands.put(userId, command);
                return command.execute(message);
            } else {
                // Display the main menu with all the commands
                return "Unknown command: " + userMessage;
            }
        }
    }
}
