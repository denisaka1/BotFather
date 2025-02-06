package org.example.botfather.telegrambot;

import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AllArgsConstructor
public class DynamicBot extends TelegramLongPollingBot {
    private final Bot bot;
    private final DynamicBotsMessageHandler dynamicBotsMessageHandler;

    @Override
    public String getBotUsername() { return bot.getUsername(); }

    @Override
    public String getBotToken() {
        return bot.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String response = dynamicBotsMessageHandler.processMessage(bot, update.getMessage());
            String chatId = update.getMessage().getChatId().toString();
            sendMessage(chatId, response);
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

