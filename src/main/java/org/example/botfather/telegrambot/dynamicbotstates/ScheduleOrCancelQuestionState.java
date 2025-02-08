package org.example.botfather.telegrambot.dynamicbotstates;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.telegrambot.DynamicBotsMessageHandler;
import org.example.botfather.telegramform.ButtonsGenerator;
import org.example.botfather.telegramform.MessageGenerator;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class ScheduleOrCancelQuestionState implements IDynamicBotState {

    @Override
    public BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData) {
        String chatId = message.getChatId().toString();
        if (callbackData != null) {
            String data = callbackData.getData();
            if ("SCHEDULE".equals(data)) {
                ScheduleState scheduleState = new ScheduleState(context.getApiRequestHelper());
                context.setState(callbackData.getFrom().getId(), scheduleState);
                return scheduleState.handle(context, bot, message);
            } else if ("CANCEL".equals(data)) {
                return new SendMessage(chatId, "You've selected: Delete an existing appointment.");
            } else if ("BACK".equals(data)) {
                return createScheduleOrCancelButtons(chatId, bot, message, true);
            }
        }
        return createScheduleOrCancelButtons(chatId, bot, message, false);
    }

    private BotApiMethod<?> createScheduleOrCancelButtons(String chatId, Bot bot, Message message, boolean isBack) {
        String text = bot.getWelcomeMessage() + "\n\n" + "What would you like to do?";

        // Create inline keyboard with two rows
        String[][] buttonConfigs = {
                {"📅 Schedule An Appointment:SCHEDULE"},
                {"❌ Cancel An Existing Appointment:CANCEL"}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfigs);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        if (isBack) {
            return MessageGenerator.createEditMessageWithMarkup(chatId, text, markup, message.getMessageId());
        } else {
            return MessageGenerator.createSendMessageWithMarkup(chatId, text, markup);
        }
    }
}
