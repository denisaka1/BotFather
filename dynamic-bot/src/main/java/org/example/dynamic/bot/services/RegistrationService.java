package org.example.dynamic.bot.services;

import lombok.extern.slf4j.Slf4j;
import org.example.data.layer.entities.Bot;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RegistrationService {
    private final TelegramBotsApi telegramBotsApi;
    private final Map<String, DynamicBot> activeBots;
    private final ApplicationContext context; // Get new bot instances from Spring

    public RegistrationService(ApplicationContext context) throws Exception {
        this.context = context;
        this.activeBots = new ConcurrentHashMap<>();
        this.telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    }

    public void registerBot(Bot tmpBot) {
        if (tmpBot == null) {
            log.warn("Trying to register a null Bot!");
            return;
        }
        if (tmpBot.getToken() == null) {
            log.warn("Trying to register a null Token for Bot: id={}", tmpBot.getId());
            return;
        }
        if (tmpBot.getUsername() == null) {
            log.warn("Trying to register a null Username for Bot: id={}", tmpBot.getId());
            return;
        }

        if (activeBots.containsKey(tmpBot.getToken())) {
            log.info("Bot already registered: {}", tmpBot.getUsername());
            return;
        }

        try {
            DynamicBot bot = context.getBean(DynamicBot.class); // Create a new instance
            bot.initialize(tmpBot);
            telegramBotsApi.registerBot(bot);
            activeBots.put(tmpBot.getToken(), bot);
            log.info("Listening for bot: {}", tmpBot.getUsername());
        } catch (Exception e) {
            log.error("Failed to register bot {}: {}", tmpBot.getUsername(), e.getMessage());
        }
    }
}
