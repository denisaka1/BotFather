package org.example.bots.manager.actions;

import lombok.RequiredArgsConstructor;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
public class BotsSlashCommand implements ISlashCommand {

    private final BusinessOwnerApi businessOwnerApi;
    private Long userId;

    public SendMessage execute(Message message) {
        userId = message.getFrom().getId();
        if (!botsExist(userId)) {
            String text = """
                    👋 Welcome to the Bots Creator!
                    You don't have any bots created.
                    You need to register using the /create command to create a new bot.
                    Type any text to return to the menu.""";
            return SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .build();
        }

        return showBotsList(message);
    }

    public SendMessage processCallbackResponse(Update update) {

        return null;
    }

    private SendMessage showBotsList(Message message) {
//        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
//        return null;
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(renderBotsInfo(message.getChatId()))
                .build();
    }

    private SendMessage renderBotsKeyboard() {

        return null;
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

    private boolean hasCallback(Update update) {
        return update.hasCallbackQuery();
    }
}

