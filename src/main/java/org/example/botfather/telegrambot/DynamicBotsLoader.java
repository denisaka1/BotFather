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
    private final DynamicBotsRegistryService botManager;
    private final ApiRequestHelper apiRequestHelper;

    @Override
    public void run(String... args) {
//        List<Bot> bots = botRepository.findAll();
        Bot bot = createBot("Lidar310225bot", "8011441952:AAGiV-aSOpv5LoE7PtEK3GvD-oGRVTIE3Nc");
        List<Bot> bots = List.of(bot);
        System.out.println("Found " + bots.size() + " bots");
//        for (Bot bot : bots) {
            botManager.registerBot(bot.getUsername(), bot.getToken());
//        }
    }

    public Bot createBot(String name, String token) {
        Bot bot = Bot.builder()
                .username(name)
                .token(token)
                .build();
        return apiRequestHelper.post("http://localhost:8080/api/bots", bot, Bot.class);
    }
}
