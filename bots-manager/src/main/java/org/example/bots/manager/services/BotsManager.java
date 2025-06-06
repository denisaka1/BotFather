package org.example.bots.manager.services;

import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.config.ConfigLoader;
import org.example.client.api.processor.MessageBatchProcessor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class BotsManager extends TelegramLongPollingBot {
    private final ManagerMessageService managerMessageService;
    private final MessageBatchProcessor messageBatchProcessor;
    private final ConfigLoader configLoader;

    public BotsManager(ManagerMessageService managerMessageService, ConfigLoader configLoader, MessageBatchProcessor messageBatchProcessor) {
        super(configLoader.getToken());
        this.managerMessageService = managerMessageService;
        this.configLoader = configLoader;
        this.messageBatchProcessor = messageBatchProcessor;
    }

    @Override
    public String getBotUsername() {
        return configLoader.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            managerMessageService.processCallbackCommand(update);
        } else if (isTextMessage(update)) {
            managerMessageService.processTextMessage(update);
        } else {
            messageBatchProcessor.addMessage(new SendMessage(getChatId(update), "I only understand text messages."));
        }
        sendMessage();
    }

    public void sendMessage() {
        try {
            for (DeleteMessage deleteMessage : messageBatchProcessor.getAllDeleteMessage())
                execute(deleteMessage);

            for (SendMessage sendMessage : messageBatchProcessor.getAllMessages())
                execute(sendMessage);

            for (EditMessageText editMessageText : messageBatchProcessor.getAllTextUpdates())
                execute(editMessageText);

            for (EditMessageReplyMarkup editMessageReplyMarkup : messageBatchProcessor.getAllButtonUpdates())
                execute(editMessageReplyMarkup);

            messageBatchProcessor.clear();
        } catch (TelegramApiException e) {
            messageBatchProcessor.clear();
            log.error("Failed to send message: {}", e.getMessage(), e);
        }
    }

    private boolean isTextMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    private String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return "Unknown Chat";
    }
}

