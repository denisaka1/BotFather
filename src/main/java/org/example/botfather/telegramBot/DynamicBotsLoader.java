package org.example.botfather.telegramBot;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.repositories.BotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DynamicBotsLoader implements CommandLineRunner {
    private final DynamicBotsRegistryService botManager;
    private final BotRepository botRepository;

    public DynamicBotsLoader(DynamicBotsRegistryService botManager, BotRepository botRepository) {
        this.botManager = botManager;
        this.botRepository = botRepository;
    }

    @Override
    public void run(String... args) {
//        List<Bot> bots = botRepository.findAll();
        List<Bot> bots = List.of(
                new Bot("Lidar310225bot", "8011441952:AAGiV-aSOpv5LoE7PtEK3GvD-oGRVTIE3Nc")
        );
        System.out.println("Found " + bots.size() + " bots");
        for (Bot bot : bots) {
            botManager.registerBot(bot.getUsername(), bot.getToken());
        }
    }
}
