package org.example.telegram.bot.telegrambot.dynamicbotstates;

import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.bot.data.entities.Bot;
import org.example.telegram.bot.telegrambot.DynamicBotsMessageHandler;
import org.example.telegram.bot.telegramcomponents.CalendarKeyboardGenerator;
import org.example.telegram.bot.telegramcomponents.MessageGenerator;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;

public class ScheduleState implements IDynamicBotState {
    private final ApiRequestHelper apiRequestHelper;

    public ScheduleState(ApiRequestHelper apiRequestHelper) {
        this.apiRequestHelper = apiRequestHelper;
    }

    @Override
    public BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData) {
        Long chatId = message.getChatId();

        if (callbackData != null) {
            return handleCallbackQuery(context, bot, message, callbackData.getData());
        }

        return sendCalendar(chatId, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message);
    }

    private BotApiMethod<?> handleCallbackQuery(DynamicBotsMessageHandler context, Bot bot, Message message, String callbackData) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        if (callbackData.startsWith("date:")) {
            String selectedDate = callbackData.split(":")[1];
            return new SendMessage(chatId.toString(), "✅ You selected: " + selectedDate);
        } else if (callbackData.startsWith("month:")) {
            String[] parts = callbackData.split(":")[1].split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month);
            return MessageGenerator.createEditMessageReplyMarkup(chatId.toString(), messageId, calendar);
        }
        return null;
    }

    private BotApiMethod<?> sendCalendar(Long chatId, int year, int month, Message message) {
        return EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(message.getMessageId())
                .text("Please select a date:")
                .replyMarkup(CalendarKeyboardGenerator.generateCalendar(year, month))
                .build();
    }


}
