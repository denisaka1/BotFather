package org.example.botfather.commands;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.utils.ApiRequestHelper;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;

@Component
public class BotsManagerBotsCommand extends AbstractBotCommand {

    public BotsManagerBotsCommand(ApiRequestHelper apiRequestHelper) {
        super(apiRequestHelper);
    }

    @Override
    public String execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!checkIfUserExists(userId)) {
            forceCompleted = true;
            return """
                    👋 Welcome to the Bots Creator!
                    You don't have any bots created.
                    You need to register using the /start command to create a new bot.
                    Type any text to return to the menu.""";
        }

        Bot[] ownerBots = getOwnerBots(userId);

        StringBuilder result = new StringBuilder("Bots information:\n");
        for (Bot bot : ownerBots) {
            result.append(bot);
        }
        forceCompleted = true;

        return result.toString();
    }

    @Override
    public boolean isCompleted() {
        return forceCompleted;
    }

    private Bot[] getOwnerBots(Long userId) {
        return apiRequestHelper.get(
                "http://localhost:8080/api/business_owner/" + userId,
                Bot[].class
        );
    }
}

