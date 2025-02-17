package org.example.bots.manager.services;

import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.config.ConfigLoader;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

@Slf4j
@Component
public class BotsManager extends TelegramLongPollingBot {
    private final ManagerMessageService managerMessageService;
    private final ConfigLoader configLoader;

    public BotsManager(ManagerMessageService managerMessageService, ConfigLoader configLoader) {
        super(configLoader.getToken());
        this.managerMessageService = managerMessageService;
        this.configLoader = configLoader;
    }

    @Override
    public String getBotUsername() {
        return configLoader.getUsername();
    }


    @Override
    public void onUpdateReceived(Update update) {
        BotApiMethod<?> response = null;
        if (update.hasCallbackQuery()) {
            response = managerMessageService.processCallbackCommand(update);
        } else if (isTextMessage(update)) {
            response = managerMessageService.processTextMessage(update);
        } else {
            response = new SendMessage(getChatId(update), "I only understand text messages.");
        }
        sendMessage(response);
    }

    public void sendMessageToUser(SendMessage message) {
        sendMessage(message);
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
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

