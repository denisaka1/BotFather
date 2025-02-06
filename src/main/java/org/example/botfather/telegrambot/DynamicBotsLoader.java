package org.example.botfather.telegrambot;
import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.utils.ApiRequestHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@AllArgsConstructor
public class DynamicBotsLoader implements CommandLineRunner {
    private final DynamicBotsRegistryService dynamicBotsRegistryService;
    private final ApiRequestHelper apiRequestHelper;

    @Override
    public void run(String... args) {
        @SuppressWarnings("unchecked")
        List<Bot> bots = apiRequestHelper.get("http://localhost:8080/api/bots", List.class);
        System.out.println("Found " + bots.size() + " bots");
        for (Bot bot : bots) {
            dynamicBotsRegistryService.registerBot(bot);
        }
    }

    public Bot createBot(String name, String token) {
        Bot bot = Bot.builder()
                .username(name)
                .token(token)
                .build();
        return apiRequestHelper.post("http://localhost:8080/api/bots", bot, Bot.class);
    }
}
