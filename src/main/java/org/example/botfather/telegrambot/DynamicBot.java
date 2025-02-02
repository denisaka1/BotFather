package org.example.botfather.telegrambot;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@AllArgsConstructor
public class DynamicBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("Bot Token: " + botToken + " received message: " + update.getMessage().getText());
        }
    }
}

