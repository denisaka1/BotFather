package org.example.botfather.telegrambot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicBotsRegistryService {
    private final TelegramBotsApi telegramBotsApi;
    private final Map<String, DynamicBot> activeBots = new ConcurrentHashMap<>();

    public DynamicBotsRegistryService() throws Exception {
        this.telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    }

    public void registerBot(String botUsername, String botToken) {
        if (activeBots.containsKey(botToken)) {
            System.out.println("Bot already registered: " + botUsername);
            return;
        }

        try {
            DynamicBot bot = new DynamicBot(botUsername, botToken);
            telegramBotsApi.registerBot(bot);
            activeBots.put(botToken, bot);
            System.out.println("Listening for bot: " + botUsername);
        } catch (Exception e) {
            System.err.println("Failed to register bot " + botUsername + ": " + e.getMessage());
        }
    }
}
