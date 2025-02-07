package org.example.botfather.telegrambot.dynamicbotstates;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.telegrambot.DynamicBotsMessageHandler;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface DynamicBotState {
    SendMessage handle(DynamicBotsMessageHandler context, Bot bot, Message message);

    default boolean isCancelCommand(Message message) {
        return message.getText().equalsIgnoreCase("/cancel");
    }

    default boolean isBackCommand(Message message) {
        return message.getText().equalsIgnoreCase("/back");
    }

    default DynamicBotState getPreviousState(DynamicBotsMessageHandler context) {
        return new ScheduleOrCancelQuestionState(); // Default fallback
    }
}

