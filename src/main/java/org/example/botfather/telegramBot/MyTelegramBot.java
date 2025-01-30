package org.example.botfather.telegramBot;
import org.example.botfather.config.ConfigLoader;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.stereotype.Component;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final MessageHandler messageHandler;
    private final ConfigLoader configLoader;

    public MyTelegramBot(MessageHandler messageHandler, ConfigLoader configLoader) {
        this.messageHandler = messageHandler;
        this.configLoader = configLoader;
    }

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
            String userMessage = update.getMessage().getText();

            String response = messageHandler.processMessage(userMessage);

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

