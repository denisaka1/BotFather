package org.example.bots.manager.services;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class MessageBatchProcessor {
    private final List<EditMessageText> textUpdates = new ArrayList<>();
    private final List<EditMessageReplyMarkup> buttonUpdates = new ArrayList<>();
    private final List<SendMessage> messages = new ArrayList<>();

    public void addTextUpdate(EditMessageText editMessageText) {
        textUpdates.add(editMessageText);
    }

    public void addButtonUpdate(EditMessageReplyMarkup editMessageReplyMarkup) {
        buttonUpdates.add(editMessageReplyMarkup);
    }

    public void addMessage(SendMessage sendMessage) {
        messages.add(sendMessage);
    }

    public void clear() {
        textUpdates.clear();
        buttonUpdates.clear();
        messages.clear();
    }
}
