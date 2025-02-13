package org.example.telegram.bot.redis.service;

import lombok.AllArgsConstructor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.telegram.bot.redis.entity.BotSession;
import org.example.telegram.bot.redis.repository.BotSessionRepository;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class BotSessionService {
    private final BotSessionRepository botSessionRepository;
    private final BotApi botApi;
    private final BusinessOwnerApi businessOwnerApi;

    public BotSession getBotSession(Long chatId) {
        return botSessionRepository.findById(chatId)
                .orElseGet(() -> {
                    Bot bot = botApi.getBot(chatId);
                    if (bot == null) {
                        return BotSession.builder().build();
                    }

                    return BotSession.builder()
                            .id(bot.getId())
                            .name(bot.getName())
                            .username(bot.getUsername())
                            .token(bot.getToken())
                            .welcomeMessage(bot.getWelcomeMessage())
                            .creationState(bot.getCreationState())
                            .build();
                });
    }

    public void saveBotSession(BotSession botSession) {
        botSessionRepository.save(botSession);
    }

    public void finalizeBotSession(Long userId, BotSession botSession) {
        Bot bot = Bot.builder()
                .name(botSession.getName())
                .username(botSession.getUsername())
                .token(botSession.getToken())
                .welcomeMessage(botSession.getWelcomeMessage())
                .creationState(BotCreationState.COMPLETED)
                .build();

        businessOwnerApi.addBot(userId, bot);
    }
}
