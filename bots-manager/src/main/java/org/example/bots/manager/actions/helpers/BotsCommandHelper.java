package org.example.bots.manager.actions.helpers;

import lombok.Getter;
import org.example.bots.manager.constants.Callback;
import org.example.bots.manager.utils.MessageExtractor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.data.layer.helpers.BotHelper;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.function.BiConsumer;

import static org.example.data.layer.entities.BotCreationState.*;

@Component
public class BotsCommandHelper {

    private final MessageBatchProcessor messageBatchProcessor;
    private final BusinessOwnerApi businessOwnerApi;
    private final BotApi botApi;

    @Getter
    private final Map<String, BiConsumer<CallbackQuery, String>> callbackHandlers = new HashMap<>();
    private final Map<BotCreationState, BiConsumer<Bot, String>> stateActions = new EnumMap<>(BotCreationState.class);

    public BotsCommandHelper(MessageBatchProcessor messageBatchProcessor, BusinessOwnerApi businessOwnerApi, BotApi botApi) {
        this.messageBatchProcessor = messageBatchProcessor;
        this.businessOwnerApi = businessOwnerApi;
        this.botApi = botApi;

        callbackHandlers.put(Callback.SELECT_BOT, this::showActions);
        callbackHandlers.put(Callback.EDIT_BOT_NAME, (query, botId) -> sendEditMessage(query, botId, ASK_BOT_NAME));
        callbackHandlers.put(Callback.EDIT_BOT_WORKING_HOURS, (query, botId) -> sendEditMessage(query, botId, ASK_WORKING_HOURS));
        callbackHandlers.put(Callback.EDIT_BOT_TOKEN, (query, botId) -> sendEditMessage(query, botId, ASK_BOT_FATHER_BOT_CREATION_MESSAGE));
        callbackHandlers.put(Callback.EDIT_BOT_WELCOME_MESSAGE, (query, botId) -> sendEditMessage(query, botId, ASK_WELCOME_MESSAGE));
        callbackHandlers.put(Callback.EDIT_BOT_JOBS, (query, botId) -> sendEditMessage(query, botId, ASK_JOBS));
        callbackHandlers.put(Callback.DELETE_BOT, this::sendDelete);
        callbackHandlers.put(Callback.BACK_TO_BOTS_LIST, (query, botId) -> returnToShowBotsList(query));

        stateActions.put(ASK_BOT_FATHER_BOT_CREATION_MESSAGE, Bot::setToken);
        stateActions.put(ASK_BOT_NAME, Bot::setName);
        stateActions.put(ASK_WELCOME_MESSAGE, Bot::setWelcomeMessage);
        stateActions.put(ASK_WORKING_HOURS, this::buildBotWorkingHours);
        stateActions.put(ASK_JOBS, this::buildBotJobs);
    }

    public boolean containsStateKey(BotCreationState state) {
        return stateActions.containsKey(state);
    }

    public void acceptState(BotCreationState currentState, Bot bot, Message message) {
        String userResponse = message.getText();
        stateActions.get(currentState).accept(bot, userResponse);
        
        bot.setCreationState(BotCreationState.COMPLETED);
        botApi.updateBot(bot);
        messageBatchProcessor.addMessage(
                MessageGenerator.createSimpleTextMessage(
                        message.getChatId(),
                        currentState.getSuccessChangeMessage()
                )
        );
    }

    public void showBotsList(Message message) {
        showBotsList(message.getFrom().getId(), false, null);
    }

    public void showBotsList(Long userId, boolean isSchedule, Integer messageId) {
        Map<String, String> config = getBotConfigList(userId, isSchedule);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(createKeyboard(config));
        String text = "Select a bot from the bots list:";
        if (isSchedule && messageId != null) {
            messageBatchProcessor.addTextUpdate(
                    MessageGenerator.createEditMessageWithMarkup(
                            userId.toString(),
                            text,
                            keyboardMarkup,
                            messageId
                    )
            );
        } else {
            messageBatchProcessor.addMessage(
                    MessageGenerator.createSendMessageWithMarkup(
                            userId.toString(),
                            text,
                            keyboardMarkup
                    )
            );
        }
    }

    public String invalidEditQuestionMessage(BotCreationState state) {
        return "❌ Invalid input!\n\n" + botEditMessageByState(state);
    }

    private void sendDelete(CallbackQuery callbackQuery, String botId) {
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

    private void sendEditMessage(CallbackQuery callbackQuery, String botId, BotCreationState state) {
        Long chatId = callbackQuery.getMessage().getChatId();

        Bot bot = botApi.getBot(botId);
        bot.setCreationState(state);
        botApi.updateBot(bot);

        messageBatchProcessor.addMessage(
                MessageGenerator.createSimpleTextMessage(chatId, botEditMessageByState(state))
        );
    }

    private void returnToShowBotsList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        Map<String, String> config = getBotConfigList(userId, false);

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

    private void showActions(CallbackQuery callbackQuery, String botId) {
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
        String text = "What would you like to do with @" + bot.getName() + "?\n" + BotHelper.info(bot);

        messageBatchProcessor.addTextUpdate(
                MessageGenerator.createEditMessageWithMarkup(
                        chatId.toString(),
                        text,
                        keyboardMarkup,
                        messageId)
        );
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

    private Map<String, String> getBotConfigList(Long userId, boolean isSchedule) {
        List<Bot> bots = businessOwnerApi.getDisplayableBots(userId);
        Map<String, String> config = new LinkedHashMap<>();
        for (Bot bot : bots) {
            String callback = isSchedule ? Callback.SELECT_APPOINTMENTS_BOT : Callback.SELECT_BOT;
            config.put("@" + bot.getUsername(), callback + bot.getId());
        }
        return config;
    }

    private String botEditMessageByState(BotCreationState botCreationState) {
        return botCreationState.getEditMessage();
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

    private void buildBotWorkingHours(Bot bot, String userResponse) {
        List<WorkingHours> workingHours = MessageExtractor.extractWorkingHours(userResponse);
        bot.setWorkingHours(workingHours);
    }

    private void buildBotJobs(Bot bot, String userResponse) {
        List<Job> jobs = MessageExtractor.extractJobs(userResponse);
        bot.setJobs(jobs);
    }
}
