package org.example.telegram.bot.telegrambot;
import lombok.extern.slf4j.Slf4j;
import org.example.data.layer.entities.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DynamicBotsRegistryService {
    private final TelegramBotsApi telegramBotsApi;
    private final Map<String, DynamicBot> activeBots;
    private final DynamicBotsMessageHandler dynamicBotsMessageHandler;

    public DynamicBotsRegistryService(DynamicBotsMessageHandler dynamicBotsMessageHandler) throws Exception {
        this.dynamicBotsMessageHandler = dynamicBotsMessageHandler;
        this.activeBots = new ConcurrentHashMap<>();
        this.telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    }

    public void registerBot(Bot tmpBot) {
        if (activeBots.containsKey(tmpBot.getToken())) {
            log.info("Bot already registered: {}", tmpBot.getUsername());
            return;
        }

        try {
            DynamicBot bot = new DynamicBot(tmpBot, dynamicBotsMessageHandler);
            telegramBotsApi.registerBot(bot);
            activeBots.put(tmpBot.getToken(), bot);
            log.info("Listening for bot: {}", tmpBot.getUsername());
        } catch (Exception e) {
            log.error("Failed to register bot {}: {}", tmpBot.getUsername(), e.getMessage());
        }
    }
}
