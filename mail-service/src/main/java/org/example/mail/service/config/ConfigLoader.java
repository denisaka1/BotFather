package org.example.mail.service.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gmail")
@Getter
@Setter
@Slf4j
public class ConfigLoader {
    private String username;
    private String password;
}
