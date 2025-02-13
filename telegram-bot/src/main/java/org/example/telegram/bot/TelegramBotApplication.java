package org.example.telegram.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(
        scanBasePackages = {
                "org.example.client.api.config",
                "org.example.client.api.helper",
                "org.example.client.api.controller",
                "org.example.data.layer.entities",
                "org.example.telegram.bot.actions",
                "org.example.telegram.bot.redis",
                "org.example.telegram.bot.config",
                "org.example.telegram.bot.polling",
                "org.example.telegram.bot.services",
                "org.example.telegram.bot.utils",

        }
)
@ConfigurationPropertiesScan
public class TelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }

}
