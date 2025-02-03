package org.example.botfather.commands;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotCommand {
    String execute(Message message);
    boolean isCompleted();
}
