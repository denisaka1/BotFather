package org.example.botfather.commands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;

@Component
public class BotsManagerBotsCommand implements BotCommand {
    @Override
    public String execute(Message message) {
        return "Here are your bots!";
    }
}

