package org.example.dynamic.bot.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.data.layer.entities.Bot;
import org.example.dynamic.bot.services.RegistrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@AllArgsConstructor
public class BotsLoader implements CommandLineRunner {
    private final RegistrationService registrationService;
    private final BotApi botApi;

    @Override
    public void run(String... args) {
        List<Bot> bots = List.of(botApi.getBots());
        log.info("Found {} bots", bots.size());
        for (Bot bot : bots) {
            registrationService.registerBot(bot);
        }
    }
}
