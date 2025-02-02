package org.example.botfather.telegrambot;
import org.springframework.stereotype.Service;

@Service
public class MessageHandler {

    public String processMessage(String message) {
        if (message.equalsIgnoreCase("/start")) {
            return "Welcome to our bot!";
        } else if (message.equalsIgnoreCase("/bots")) {
            // need to know the identifier for business owner
            return "Here are your bots!";
        }
        return "Got " + message;
    }

}
