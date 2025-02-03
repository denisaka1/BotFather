package org.example.botfather.commands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;

@Component
public class BotsManagerStartCommand implements BotCommand {
    @Override
    public String execute(Message message) {
        // check if the user is already registered
        Long userId = message.getFrom().getId();

        return "Welcome to our bot!";
    }
}
