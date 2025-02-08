package org.example.botfather.telegrambot.dynamicbotstates;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.telegrambot.DynamicBotsMessageHandler;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface DynamicBotState {
    BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData);

    default BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message) {
        return handle(context, bot, message, null); // Pass null when callbackData isn't provided
    }

    default boolean isBackCommand(CallbackQuery callbackData) {
        return callbackData.getData().equalsIgnoreCase("BACK");
    }

    default DynamicBotState getPreviousState(DynamicBotsMessageHandler context) {
        return new ScheduleOrCancelQuestionState(); // Default fallback
    }
}

