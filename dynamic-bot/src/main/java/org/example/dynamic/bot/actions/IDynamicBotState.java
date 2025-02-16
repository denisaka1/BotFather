package org.example.dynamic.bot.actions;
import org.example.data.layer.entities.Bot;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface IDynamicBotState {
    BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData);

    default BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message) {
        return handle(context, bot, message, null); // Pass null when callbackData isn't provided
    }

    default boolean isBackCommand(CallbackQuery callbackData) {
        return callbackData.getData().equalsIgnoreCase("BACK");
    }

    default IDynamicBotState getPreviousState(DynamicMessageService context) {
        return context.getScheduleOrCancelQuestionState(); // Default fallback
    }
}

