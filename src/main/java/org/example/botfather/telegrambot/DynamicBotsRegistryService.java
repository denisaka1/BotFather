package org.example.botfather.telegrambot;
import org.example.botfather.data.entities.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicBotsRegistryService {
    private final TelegramBotsApi telegramBotsApi;
    private final Map<String, DynamicBot> activeBots = new ConcurrentHashMap<>();
    private final DynamicBotsMessageHandler dynamicBotsMessageHandler;

    public DynamicBotsRegistryService(DynamicBotsMessageHandler dynamicBotsMessageHandler) throws Exception {
        this.dynamicBotsMessageHandler = dynamicBotsMessageHandler;
        this.telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    }

    public void registerBot(Bot tmpBot) {
        if (activeBots.containsKey(tmpBot.getToken())) {
            System.out.println("Bot already registered: " + tmpBot.getUsername());
            return;
        }

        try {
            DynamicBot bot = new DynamicBot(tmpBot, dynamicBotsMessageHandler);
            telegramBotsApi.registerBot(bot);
            activeBots.put(tmpBot.getToken(), bot);
            System.out.println("Listening for bot: " + tmpBot.getUsername());
        } catch (Exception e) {
            System.err.println("Failed to register bot " + tmpBot.getUsername() + ": " + e.getMessage());
        }
    }
}
