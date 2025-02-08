package org.example.telegram.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(
        scanBasePackages = {
                "org.example.client.api.config",
                "org.example.client.api.helper",
                "org.example.telegram.bot.config",
                "org.example.telegram.bot.commands",
                "org.example.telegram.bot.telegrambot",
                "org.example.telegram.bot.controllers",
                "org.example.telegram.bot.data",
                "org.example.telegram.bot.utils",

        }
)
@ConfigurationPropertiesScan
public class TelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }

}
