package org.example.botfather.telegrambot.dynamicbotstates;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.telegrambot.DynamicBotsMessageHandler;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleOrCancelQuestionState implements DynamicBotState {

    @Override
    public SendMessage handle(DynamicBotsMessageHandler context, Bot bot, Message message) {
        if (isCancelCommand(message) || isBackCommand(message)) {
            return new SendMessage(message.getChatId().toString(), "You're already in the scheduling options.");
        }
        return createMenu(message.getChatId().toString(), bot);
    }

    private SendMessage createMenu(String chatId, Bot bot) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(bot.getWelcomeMessage() + "\n\n" + "What would you like to do?");

        // Create inline keyboard with two rows
        List<List<InlineKeyboardButton>> keyboard = getInlineKeyboardButtons();

        // Attach keyboard to message
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    private static List<List<InlineKeyboardButton>> getInlineKeyboardButtons() {
        InlineKeyboardButton scheduleButton = new InlineKeyboardButton();
        scheduleButton.setText("📅 Schedule An Appointment");
        scheduleButton.setCallbackData("SCHEDULE");

        InlineKeyboardButton editButton = new InlineKeyboardButton();
        editButton.setText("❌ Cancel An Existing Appointment");
        editButton.setCallbackData("EDIT");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(Collections.singletonList(scheduleButton)); // Row 1
        keyboard.add(Collections.singletonList(editButton));     // Row 2

        return keyboard;
    }
}
