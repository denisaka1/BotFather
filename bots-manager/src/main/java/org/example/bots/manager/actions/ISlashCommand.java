package org.example.bots.manager.actions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ISlashCommand {
    BotApiMethod<?> execute(Message message);
}
