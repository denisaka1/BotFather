package org.example.bots.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(
        scanBasePackages = {
                "org.example.client.api.config",
                "org.example.client.api.helper",
                "org.example.client.api.controller",
                "org.example.data.layer.entities",
                "org.example.bots.manager.config",
                "org.example.bots.manager.actions",
                "org.example.bots.manager.controllers",
                "org.example.bots.manager.services",
                "org.example.bots.manager.utils",
        }
)
@ConfigurationPropertiesScan
public class BotsManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotsManagerApplication.class, args);
    }

}
