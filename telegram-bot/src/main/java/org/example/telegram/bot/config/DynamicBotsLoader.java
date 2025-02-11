package org.example.telegram.bot.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.telegram.bot.services.dynamic.RegistrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
//@Component
@Configuration
@AllArgsConstructor
public class DynamicBotsLoader implements CommandLineRunner {
    private final RegistrationService registrationService;
    private final ApiRequestHelper apiRequestHelper;

    @Override
    public void run(String... args) {
        List<Bot> bots = List.of(apiRequestHelper.get("http://localhost:8080/api/bots", Bot[].class));
        log.info("Found {} bots", bots.size());
        for (Bot bot : bots) {
            registrationService.registerBot(bot);
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
