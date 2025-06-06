package org.example.dynamic.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {
                "org.example.client.api.config",
                "org.example.client.api.helper",
                "org.example.mail.service.services",
                "org.example.mail.service.config",
                "org.example.client.api.controller",
                "org.example.client.api.processor",
                "org.example.data.layer.entities",
                "org.example.dynamic.bot.config",
                "org.example.dynamic.bot.actions",
                "org.example.dynamic.bot.controllers",
                "org.example.dynamic.bot.services",
                "org.example.dynamic.bot.scheduler",

        }
)
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class DynamicBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamicBotApplication.class, args);
    }
}
