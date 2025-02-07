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
    public String getBotUsername() {
        return bot.getUsername();
    }

    @Override
    public String getBotToken() {
        return bot.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage response = dynamicBotsMessageHandler.processMessage(bot, update);
        sendMessage(response);
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

