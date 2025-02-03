package org.example.botfather.commands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;

@Component
public class BotsManagerCreateCommand implements BotCommand {
    @Override
    public String execute(Message message) {
        return "Create new bot!";
    }

    @Override
    public boolean isCompleted() {
        return false;
    }
}

