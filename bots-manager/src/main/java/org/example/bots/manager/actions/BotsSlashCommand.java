package org.example.bots.manager.actions;

import lombok.RequiredArgsConstructor;
import org.example.bots.manager.services.MessageBatchProcessor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.Bot;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
    private final MessageBatchProcessor messageBatchProcessor;
    private final BotApi botApi;

    public void execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!botsExist(userId)) {
            String text = """
                    👋 Welcome to the Bots Creator!
                    You don't have any bots created.
                    You need to register using the /create command to create a new bot.
                    Type any text to return to the menu.""";
            messageBatchProcessor.addMessage(
                    SendMessage.builder()
                            .chatId(userId)
                            .text(text)
                            .build()
            );
            return;
        }

        messageBatchProcessor.addMessage(showBotsList(message));
    }

    public void processCallbackResponse(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (callbackData.startsWith("select_bot_")) {
            String botId = callbackData.replace("select_bot_", "");
            showBotActions(update.getCallbackQuery(), botId);
        }
    }

    private void showBotActions(CallbackQuery callbackQuery, String botId) {
        String callbackData = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long chatId = callbackQuery.getMessage().getChatId();

        Map<String, String> config = new LinkedHashMap<>();
        config.put("✏️ Edit Name", "edit_name_" + botId);
        config.put("✏️ Edit Username", "edit_token_" + botId);
        config.put("✏️ Edit Working Hours", "edit_working_hours_" + botId);
        config.put("\uD83D\uDD11 Edit Token", "edit_token_" + botId);
        config.put("\uD83D\uDC4B Edit Welcome Message", "edit_welcome_message_" + botId);
        config.put("❌ Cancel Appointment", "cancel_appointment_" + botId);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = createKeyboard(config);
        keyboard.add(backToBotsListButton());
        keyboardMarkup.setKeyboard(keyboard);

        messageBatchProcessor.addTextUpdate(
                MessageGenerator.createEditMessageWithMarkup(
                        chatId.toString(),
                        "What would you like to do with the bot?",
                        keyboardMarkup,
                        messageId)
        );
    }

    private SendMessage showBotsList(Message message) {
        Long userId = message.getFrom().getId();

        List<Bot> bots = List.of(businessOwnerApi.getBots(userId));
        Map<String, String> config = new LinkedHashMap<>();
        for (Bot bot : bots) {
            config.put("@" + bot.getUsername(), "select_bot_" + bot.getId());
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(createKeyboard(config));
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
//        keyboard.add(cancelActionButton());

        return keyboard;
    }

    private List<InlineKeyboardButton> backToBotsListButton() {
        List<InlineKeyboardButton> backToBotsListButton = new ArrayList<>();
        backToBotsListButton.add(ButtonsGenerator.createButton(
                "\uD83D\uDD19 Back",
                "back_to_bots_list")
        );
        return backToBotsListButton;
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

