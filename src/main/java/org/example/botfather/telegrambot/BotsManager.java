package org.example.botfather.telegrambot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.botfather.config.ConfigLoader;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class BotsManager extends TelegramLongPollingBot {

    private final BotsManagerMessageHandler botsManagerMessageHandler;
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
            String chatId = update.getMessage().getChatId().toString();

            String response = botsManagerMessageHandler.processMessage(update.getMessage());

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
            log.error("Failed to send message {}, error: {}", message, e.getMessage());
        }
    }
}

