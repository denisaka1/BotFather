package org.example.telegram.bot.actions.manager;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ISlashCommand {
    String execute(Message message);
}
