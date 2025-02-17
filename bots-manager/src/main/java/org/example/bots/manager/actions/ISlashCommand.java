package org.example.bots.manager.actions;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface ISlashCommand {
    void execute(Message message);
}
