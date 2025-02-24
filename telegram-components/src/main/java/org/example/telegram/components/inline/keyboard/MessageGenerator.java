package org.example.telegram.components.inline.keyboard;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageGenerator {

    private MessageGenerator() {
    }

    public static EditMessageText createEditMessageWithMarkup(String chatId, String text, InlineKeyboardMarkup markup, Integer messageId) {
        return EditMessageText.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(markup)
                .messageId(messageId)
                .build();
    }

    public static EditMessageText createEditMessage(Long chatId, String text, Integer messageId) {
        return EditMessageText.builder()
                .chatId(chatId)
                .text(text)
                .messageId(messageId)
                .build();
    }

    public static DeleteMessage deleteMessage(String chatId, Integer messageId) {
        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public static DeleteMessage deleteMessage(Long chatId, Integer messageId) {
        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public static SendMessage createSendMessageWithMarkup(String chatId, String text, InlineKeyboardMarkup markup) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(markup)
                .build();
    }

    public static EditMessageReplyMarkup createEditMessageReplyMarkup(String chatId, Integer messageId, InlineKeyboardMarkup markup) {
        return EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(markup)
                .build();
    }

    public static SendMessage createSimpleTextMessage(Long chatId, String text) {
        return SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
    }

    public static SendMessage createSimpleTextMessage(String chatId, String text) {
        return SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
    }
}
