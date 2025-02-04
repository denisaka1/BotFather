package org.example.botfather.data.services;
import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.repositories.BotRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class BotService {

    private final BotRepository botRepository;

    public List<Bot> getBots() {
        return botRepository.findAll();
    }

    public Bot saveBot(Bot bot) {
        // register the bot with DynamicBotsRegistryService to telemgrambotapi with the token and username
        return botRepository.save(bot);
    }
}
