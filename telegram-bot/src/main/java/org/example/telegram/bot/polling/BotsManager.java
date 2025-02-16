package org.example.telegram.bot.polling;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegram.bot.config.ConfigLoader;
import org.example.telegram.bot.services.manager.ManagerMessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@AllArgsConstructor
public class BotsManager extends TelegramLongPollingBot {

    private final ManagerMessageService managerMessageService;
    private final ConfigLoader configLoader;

    @Override
    public String getBotUsername() {
        return configLoader.getUsername();
    }

    @Override
    public String getBotToken() {
        return configLoader.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage response = managerMessageService.processMessage(update);
            sendMessage(response);
        }
    }

    public void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

