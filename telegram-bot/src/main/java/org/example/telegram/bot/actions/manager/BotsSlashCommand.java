package org.example.telegram.bot.actions.manager;

import lombok.AllArgsConstructor;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@AllArgsConstructor
@Component
public class BotsSlashCommand implements ISlashCommand {

    private final BusinessOwnerApi businessOwnerApi;

    public String execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!botsExist(userId)) {
            return """
                    👋 Welcome to the Bots Creator!
                    You don't have any bots created.
                    You need to register using the /create command to create a new bot.
                    Type any text to return to the menu.""";
        }

        return renderBotsInfo(userId);
    }

    private String renderBotsInfo(Long userId) {
        Bot[] ownerBots = businessOwnerApi.getBots(userId);

        StringBuilder result = new StringBuilder("Bots information:\n\n");
        for (int i = 0; i < ownerBots.length; i++) {
            result.append("Bot ").append(i + 1).append(":\n");
            result.append(ownerBots[i].botInfo());
            result.append("\n\n");
        }

        return result.toString();
    }

    private boolean botsExist(Long userId) {
        return businessOwnerApi.isRegistered(userId) && businessOwnerApi.getBots(userId).length > 0;
    }

    public boolean isCompleted() {
        return true;
    }

    public SendMessage processUserResponse(Update update) {
        if (hasCallback(update)) {
            return processCallbackResponse(update);
        }

        
        return null;
    }

    private SendMessage processCallbackResponse(Update update) {
        return null;
    }

    private boolean hasCallback(Update update) {
        return update.hasCallbackQuery();
    }
}

