package org.example.botfather.telegramBot;
import org.springframework.stereotype.Service;

@Service
public class MessageHandler {

    public String processMessage(String message) {
        switch (message.toLowerCase()) {
            case "/start":
                return "Welcome to our bot!";
            default:
                return "Got " + message;
        }
    }
}
