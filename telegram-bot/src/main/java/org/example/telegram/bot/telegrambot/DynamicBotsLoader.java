package org.example.telegram.bot.telegrambot;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.bot.data.entities.Bot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class DynamicBotsLoader implements CommandLineRunner {
    private final DynamicBotsRegistryService dynamicBotsRegistryService;
    private final ApiRequestHelper apiRequestHelper;

    @Override
    public void run(String... args) {
        List<Bot> bots = List.of(apiRequestHelper.get("http://localhost:8080/api/bots", Bot[].class));
        log.info("Found {} bots", bots.size());
        for (Bot bot : bots) {
            dynamicBotsRegistryService.registerBot(bot);
        }
    }

    public Bot createBot(String username, String token) {
        Bot bot = Bot.builder()
                .username(username)
                .token(token)
                .build();
        return apiRequestHelper.post("http://localhost:8080/api/bots", bot, Bot.class);
    }
}
