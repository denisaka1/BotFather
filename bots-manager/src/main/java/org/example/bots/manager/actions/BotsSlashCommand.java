package org.example.bots.manager.actions;

import lombok.RequiredArgsConstructor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.Bot;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class BotsSlashCommand implements ISlashCommand {

    private final BusinessOwnerApi businessOwnerApi;
    private final BotApi botApi;

    public SendMessage execute(Message message) {
        Long userId = message.getFrom().getId();
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
        if (update.getCallbackQuery().getData().startsWith("select_bot_")) {
//            return showBotActions(update);
        }
        return null;
    }

    private EditMessageReplyMarkup showBotActions(Update update) {
        return null;
//        EditMessageReplyMarkup
    }

    private SendMessage showBotsList(Message message) {
        Long userId = message.getFrom().getId();

        List<Bot> bots = List.of(businessOwnerApi.getBots(userId));
        Map<String, String> config = new LinkedHashMap<>();
        for (Bot bot : bots) {
            config.put("@" + bot.getUsername(), "select_bot_" + bot.getId());
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(createShowBotKeyboard(config));
        return MessageGenerator.createSendMessageWithMarkup(
                userId.toString(),
                "Select a bot from the bots list:",
                keyboardMarkup
        );
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

    private List<List<InlineKeyboardButton>> createShowBotKeyboard(Map<String, String> buttonConfigs) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (Map.Entry<String, String> config : buttonConfigs.entrySet()) {
            row.add(ButtonsGenerator.createButton(config.getKey(), config.getValue()));

            if (row.size() == 2) {
                keyboard.add(new ArrayList<>(row));
                row.clear();
            }
        }
        if (!row.isEmpty()) {
            keyboard.add(row);
        }
//        keyboard.add(cancelActionButton());

        return keyboard;
    }

//    private List<InlineKeyboardButton> cancelActionButton() {
//        List<InlineKeyboardButton> cancelButton = new ArrayList<>();
//        cancelButton.add(InlineKeyboardButton.builder()
//                .text("❌ Cancel")
//                .callbackData("cancel_bots_action")
//                .build());
//        return cancelButton;
//    }
}

