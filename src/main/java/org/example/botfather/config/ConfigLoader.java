package org.example.botfather.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class ConfigLoader {

    private String username;
    private String token;
}