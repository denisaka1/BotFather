package org.example.client.api.processor;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Component
public class MessageBatchProcessor {
    private final Map<Long, List<EditMessageText>> textUpdates = new HashMap<>();
    private final Map<Long, List<EditMessageReplyMarkup>> buttonUpdates = new HashMap<>();
    private final Map<Long, List<SendMessage>> messages = new HashMap<>();
    private final Map<Long, List<DeleteMessage>> deleteMessages = new HashMap<>();

    public void addTextUpdate(EditMessageText editMessageText) {
        Long chatId = Long.valueOf(editMessageText.getChatId());
        textUpdates.computeIfAbsent(chatId, k -> new ArrayList<>()).add(editMessageText);
    }

    public List<EditMessageText> getAllTextUpdates() {
        return textUpdates.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public void addDeleteMessage(DeleteMessage deleteMessage) {
        Long chatId = Long.valueOf(deleteMessage.getChatId());
        deleteMessages.computeIfAbsent(chatId, k -> new ArrayList<>()).add(deleteMessage);
    }

    public void addButtonUpdate(EditMessageReplyMarkup editMessageReplyMarkup) {
        Long chatId = Long.valueOf(editMessageReplyMarkup.getChatId());
        buttonUpdates.computeIfAbsent(chatId, k -> new ArrayList<>()).add(editMessageReplyMarkup);
    }

    public List<EditMessageReplyMarkup> getAllButtonUpdates() {
        return buttonUpdates.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public void addMessage(SendMessage sendMessage) {
        Long chatId = Long.valueOf(sendMessage.getChatId());
        messages.computeIfAbsent(chatId, k -> new ArrayList<>()).add(sendMessage);
    }

    public List<SendMessage> getAllMessages() {
        return messages.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public void clear(Long chatId) {
        textUpdates.remove(chatId);
        buttonUpdates.remove(chatId);
        messages.remove(chatId);
        deleteMessages.remove(chatId);
    }

    public void clear() {
        textUpdates.clear();
        buttonUpdates.clear();
        messages.clear();
    }
}
