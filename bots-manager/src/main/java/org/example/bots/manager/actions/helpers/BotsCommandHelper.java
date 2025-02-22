package org.example.bots.manager.actions.helpers;

import lombok.RequiredArgsConstructor;
import org.example.bots.manager.constants.Callback;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BotsCommandHelper {

    private final MessageBatchProcessor messageBatchProcessor;
    private final BusinessOwnerApi businessOwnerApi;
    private final BotApi botApi;

    public void sendDelete(CallbackQuery callbackQuery, String botId) {
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Bot bot = businessOwnerApi.deleteBot(userId, botId);

        messageBatchProcessor.addMessage(
                MessageGenerator.createSimpleTextMessage(
                        chatId,
                        bot.getName() + " has been deleted."
                )
        );

        if (!businessOwnerApi.getDisplayableBots(userId).isEmpty()) {
            returnToShowBotsList(callbackQuery);
        } else {
            noBotsToShow(callbackQuery);
        }
    }

    public void sendEditMessage(CallbackQuery callbackQuery, String botId, BotCreationState state) {
        Long chatId = callbackQuery.getMessage().getChatId();

        Bot bot = botApi.getBot(botId);
        bot.setCreationState(state);
        botApi.updateBot(bot);

        messageBatchProcessor.addMessage(
                MessageGenerator.createSimpleTextMessage(chatId, botEditMessageByState(state))
        );
    }

    public void returnToShowBotsList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        Map<String, String> config = getBotConfigList(userId);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        if (config.isEmpty()) {
            // TODO: delete the buttons message
            // Send:
//            👋 Welcome to the Bots Creator!
//                    You don't have any bots created.
//            You need to register using the /create command to create a new bot.
//                    Type any text to return to the menu.
        } else {
            keyboardMarkup.setKeyboard(createKeyboard(config));
            messageBatchProcessor.addTextUpdate(
                    MessageGenerator.createEditMessageWithMarkup(
                            chatId.toString(),
                            "Select a bot from the bots list:",
                            keyboardMarkup,
                            messageId)
            );
        }
    }

    public void showBotsList(Message message) {
        Long userId = message.getFrom().getId();

        Map<String, String> config = getBotConfigList(userId);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(createKeyboard(config));
        messageBatchProcessor.addMessage(
                MessageGenerator.createSendMessageWithMarkup(
                        userId.toString(),
                        "Select a bot from the bots list:",
                        keyboardMarkup
                )
        );
    }

    public void showActions(CallbackQuery callbackQuery, String botId) {
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long chatId = callbackQuery.getMessage().getChatId();

        Map<String, String> config = new LinkedHashMap<>();
        config.put("✏️ Edit Name", Callback.EDIT_BOT_NAME + botId);
        config.put("✏️ Edit Working Hours", Callback.EDIT_BOT_WORKING_HOURS + botId);
        config.put("\uD83D\uDD11 Edit Token", Callback.EDIT_BOT_TOKEN + botId);
        config.put("\uD83D\uDC4B Edit Welcome Message", Callback.EDIT_BOT_WELCOME_MESSAGE + botId);
        config.put("✏️ Edit Jobs", Callback.EDIT_BOT_JOBS + botId);
        config.put("\uD83D\uDCA3 Delete Bot", Callback.DELETE_BOT + botId);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = createKeyboard(config);

        keyboard.add(backToBotsListButton());
        keyboardMarkup.setKeyboard(keyboard);

        Bot bot = botApi.getBot(botId);
        String text = "What would you like to do with @" + bot.getName() + "?\n" + bot.info();

        messageBatchProcessor.addTextUpdate(
                MessageGenerator.createEditMessageWithMarkup(
                        chatId.toString(),
                        text,
                        keyboardMarkup,
                        messageId)
        );
    }

    public String invalidEditQuestionMessage(BotCreationState state) {
        return "❌ Invalid input!\n\n" + botEditMessageByState(state);
    }

    private List<List<InlineKeyboardButton>> createKeyboard(Map<String, String> buttonConfigs) {
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

        return keyboard;
    }

    private List<InlineKeyboardButton> backToBotsListButton() {
        List<InlineKeyboardButton> backToBotsListButton = new ArrayList<>();
        backToBotsListButton.add(ButtonsGenerator.createButton(
                "\uD83D\uDD19 Back",
                Callback.BACK_TO_BOTS_LIST)
        );
        return backToBotsListButton;
    }

    private Map<String, String> getBotConfigList(Long userId) {
        List<Bot> bots = businessOwnerApi.getDisplayableBots(userId);
        Map<String, String> config = new LinkedHashMap<>();
        for (Bot bot : bots) {
            config.put("@" + bot.getUsername(), Callback.SELECT_BOT + bot.getId());
        }
        return config;
    }

    private String botEditMessageByState(BotCreationState botCreationState) {
        switch (botCreationState) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> {
                return "What is the new token for the bot?";
            }
            case ASK_BOT_NAME -> {
                return "What is the new name for the bot?";
            }
            case ASK_WELCOME_MESSAGE -> {
                return "What is the new welcome message for the bot?";
            }
            case ASK_WORKING_HOURS -> {
                return "What are the new working hours for the bot?";
            }
            case ASK_JOBS -> {
                return "What are the new jobs for the bot?";
            }
            default -> {
                return "Unknown state";
            }
        }
    }

    private void noBotsToShow(CallbackQuery callbackQuery) {
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long chatId = callbackQuery.getMessage().getChatId();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(backToBotsListButton());
        keyboardMarkup.setKeyboard(keyboard);

        String text = "You have deleted all your bots!";

        // delete the inline keyboard
        messageBatchProcessor.addDeleteMessage(
                MessageGenerator.deleteMessage(
                        chatId.toString(),
                        messageId
                )
        );

        messageBatchProcessor.addMessage(
                MessageGenerator.createSimpleTextMessage(
                        chatId,
                        text
                )
        );
    }
}
