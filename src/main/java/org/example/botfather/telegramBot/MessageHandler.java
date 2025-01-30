package org.example.botfather.telegramBot;
import org.springframework.stereotype.Service;

@Service
public class MessageHandler {

    public String processMessage(String message) {
        if (message.equalsIgnoreCase("/start")) {
            return "Welcome to our bot!";
        } else {
            return "Got " + message;
        }
    }
}
