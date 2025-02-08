package org.example.telegram.bot.telegrambot.dynamicbotstates;
import org.example.telegram.bot.data.entities.Bot;
import org.example.telegram.bot.telegrambot.DynamicBotsMessageHandler;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface IDynamicBotState {
    BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData);

    default BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message) {
        return handle(context, bot, message, null); // Pass null when callbackData isn't provided
    }

    default boolean isBackCommand(CallbackQuery callbackData) {
        return callbackData.getData().equalsIgnoreCase("BACK");
    }

    default IDynamicBotState getPreviousState(DynamicBotsMessageHandler context) {
        return new ScheduleOrCancelQuestionState(); // Default fallback
    }
}

