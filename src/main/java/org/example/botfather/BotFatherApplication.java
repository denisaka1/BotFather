package org.example.botfather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BotFatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotFatherApplication.class, args);
    }

}
