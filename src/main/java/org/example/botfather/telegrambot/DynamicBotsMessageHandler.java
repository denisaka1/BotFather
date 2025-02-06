package org.example.botfather.telegrambot;
import lombok.AllArgsConstructor;
import org.example.botfather.commands.BotCommand;
import org.example.botfather.data.entities.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class DynamicBotsMessageHandler {
    private enum states { AUTH, SCHEDULE, EDIT, DONE };
    private final Map<Long, states> userStates = new HashMap<>();


    public String processMessage(Bot bot, Message message) {
        System.out.println("user id" + message.getFrom().getId());
        System.out.println("Bot Token: " + bot.getToken() + " received message: " + message.getText());
        String userMessage = message.getText().toLowerCase();
        Long userId = message.getFrom().getId();

        return bot.getWelcomeMessage();
    }
}
