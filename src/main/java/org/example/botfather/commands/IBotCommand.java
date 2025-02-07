package org.example.botfather.commands;
import org.example.botfather.telegramform.GenericForm;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;

public interface IBotCommand {
    String execute(Message message);
    boolean isCompleted();
}
