package org.example.telegram.components.inline.keyboard;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageGenerator {

    private MessageGenerator() {}

    public static EditMessageText createEditMessageWithMarkup(String chatId, String text, InlineKeyboardMarkup markup, Integer messageId) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(markup);
        return editMessage;
    }

    public static SendMessage createSendMessageWithMarkup(String chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    public static EditMessageReplyMarkup createEditMessageReplyMarkup(String chatId, Integer messageId, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(markup);
        return editMessageReplyMarkup;
    }
}
