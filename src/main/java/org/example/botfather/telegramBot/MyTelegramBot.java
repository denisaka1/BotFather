package org.example.botfather.telegramBot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.stereotype.Component;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private static final String BOT_USERNAME = "LidarAndDenisBot";
    private static final String BOT_TOKEN = "7601875212:AAFyrV-TyC2KZf7qwx1KKBsZ8ZQf6V27XQk";

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String userMessage = update.getMessage().getText();

            String response = processMessage(userMessage);

            sendMessage(chatId, response);
        }
    }

    private String processMessage(String message) {
        if (message.equalsIgnoreCase("/start")) {
            return "Welcome to our bot!";
        } else {
            return "Got " + message;
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

