package org.example.dynamic.bot.services;

import lombok.RequiredArgsConstructor;
import org.example.data.layer.entities.Bot;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DynamicBot extends TelegramLongPollingBot {
    private Bot bot;
    private final DynamicMessageService dynamicMessageService;

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
        BotApiMethod<?> response = dynamicMessageService.processMessage(bot, update);
        if (response != null) sendMessage(response);
    }

    public void handleAppointmentResponse(SendMessage message) {
        sendMessage(message);
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

