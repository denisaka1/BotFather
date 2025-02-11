package org.example.telegram.bot.polling;

import lombok.AllArgsConstructor;
import org.example.data.layer.entities.Bot;
import org.example.telegram.bot.services.dynamic.DynamicMessageService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AllArgsConstructor
public class DynamicBot extends TelegramLongPollingBot {
    private final Bot bot;
    private final DynamicMessageService dynamicMessageService;

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
        BotApiMethod<?> response = dynamicMessageService.processMessage(bot, update);
        if (response != null) sendMessage(response);
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

