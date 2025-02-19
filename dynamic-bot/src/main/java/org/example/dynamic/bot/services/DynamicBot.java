package org.example.dynamic.bot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DynamicBot extends TelegramLongPollingBot {
    private Bot bot;
    private final DynamicMessageService dynamicMessageService;
    private final MessageBatchProcessor messageBatchProcessor;

    public void initialize(Bot bot) {
        this.bot = bot;
    }

    @Override
    public String getBotUsername() {
        return bot.getUsername();
    }

    @Override
    public String getBotToken() {
        return bot.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        dynamicMessageService.processMessage(bot, update);
        sendMessage(getChatId(update));
    }

    public void handleAppointmentResponse(SendMessage message) {
        messageBatchProcessor.addMessage(message);
        sendMessage(message.getChatId());
    }

    public void sendMessage(String chatId) {
        Long chatIdValue = Long.valueOf(chatId);

        try {
            List<SendMessage> messages = messageBatchProcessor.getMessages().getOrDefault(chatIdValue, List.of());
            List<DeleteMessage> deleteMessages = messageBatchProcessor.getDeleteMessages().getOrDefault(chatIdValue, List.of());
            List<EditMessageText> textUpdates = messageBatchProcessor.getTextUpdates().getOrDefault(chatIdValue, List.of());
            List<EditMessageReplyMarkup> buttonUpdates = messageBatchProcessor.getButtonUpdates().getOrDefault(chatIdValue, List.of());

            for (DeleteMessage deleteMessage : deleteMessages) execute(deleteMessage);
            for (SendMessage sendMessage : messages) execute(sendMessage);
            for (EditMessageText editMessageText : textUpdates) execute(editMessageText);
            for (EditMessageReplyMarkup editMessageReplyMarkup : buttonUpdates) execute(editMessageReplyMarkup);

            messageBatchProcessor.clear(chatIdValue);

        } catch (TelegramApiException e) {
            messageBatchProcessor.clear(chatIdValue);
            log.error("Failed to send message for chatId {}: {}", chatIdValue, e.getMessage(), e);
        }
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

