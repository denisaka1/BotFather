package org.example.mail.service.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Getter
@Setter
@Slf4j
@Component
public class ConfigLoader {
    private final ResourceLoader resourceLoader;

    public ConfigLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        Yaml yaml = new Yaml();
        Resource resource = resourceLoader.getResource("classpath:config.yml");
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> data = yaml.load(inputStream);
            Map<String, String> gmailConfig = (Map<String, String>) data.get("gmail");
            this.username = gmailConfig.get("username");
            this.password = gmailConfig.get("password");
        } catch (IOException e) {
            log.error("Failed to load config: {}", e.getMessage());
        }
    }

    private String username;
    private String password;
}
