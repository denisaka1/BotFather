package org.example.bots.manager.actions;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ISlashCommand {
    String execute(Message message);
}
