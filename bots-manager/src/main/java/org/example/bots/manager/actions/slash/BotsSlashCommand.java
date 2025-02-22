package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.actions.helpers.BotsCommandHelper;
import org.example.bots.manager.actions.helpers.CommonCommandHelper;
import org.example.bots.manager.constants.Callback;
import org.example.bots.manager.utils.MessageExtractor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotsSlashCommand implements ISlashCommand {

    private final BusinessOwnerApi businessOwnerApi;
    private final MessageBatchProcessor messageBatchProcessor;
    private final BotApi botApi;
    private final BotsCommandHelper botsCommandHelper;
    private final CommonCommandHelper commonCommandHelper;

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

        botsCommandHelper.showBotsList(message);
    }

    public void processCallbackResponse(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Map<String, BiConsumer<CallbackQuery, String>> callbackHandlers = new HashMap<>();

        callbackHandlers.put(Callback.SELECT_BOT, botsCommandHelper::showActions);
        callbackHandlers.put(Callback.EDIT_BOT_NAME, (query, botId) -> botsCommandHelper.sendEditMessage(query, botId, BotCreationState.ASK_BOT_NAME));
        callbackHandlers.put(Callback.EDIT_BOT_WORKING_HOURS, (query, botId) -> botsCommandHelper.sendEditMessage(query, botId, BotCreationState.ASK_WORKING_HOURS));
        callbackHandlers.put(Callback.EDIT_BOT_TOKEN, (query, botId) -> botsCommandHelper.sendEditMessage(query, botId, BotCreationState.ASK_BOT_FATHER_BOT_CREATION_MESSAGE));
        callbackHandlers.put(Callback.EDIT_BOT_WELCOME_MESSAGE, (query, botId) -> botsCommandHelper.sendEditMessage(query, botId, BotCreationState.ASK_WELCOME_MESSAGE));
        callbackHandlers.put(Callback.EDIT_BOT_JOBS, (query, botId) -> botsCommandHelper.sendEditMessage(query, botId, BotCreationState.ASK_JOBS));
        callbackHandlers.put(Callback.DELETE_BOT, botsCommandHelper::sendDelete);
        callbackHandlers.put(Callback.BACK_TO_BOTS_LIST, (query, botId) -> botsCommandHelper.returnToShowBotsList(query));

        for (Map.Entry<String, BiConsumer<CallbackQuery, String>> entry : callbackHandlers.entrySet()) {
            String key = entry.getKey();

            if (callbackData.startsWith(key)) {
                String botId = callbackData.replace(key, "");
                entry.getValue().accept(update.getCallbackQuery(), botId);
                return;
            }
        }
    }

    private boolean botsExist(Long userId) {
        return businessOwnerApi.isRegistered(userId) && businessOwnerApi.getBots(userId).length > 0;
    }

    public void processUserResponse(Message message) {
        // process user message for the edit action
        String userResponse = message.getText();
        Bot bot = businessOwnerApi.getEditableBot(message.getFrom().getId());

        BotCreationState currentState = bot.getCreationState();
        if (!commonCommandHelper.isValidInput(currentState, userResponse)) {
            messageBatchProcessor.addMessage(
                    MessageGenerator.createSimpleTextMessage(
                            message.getChatId(),
                            botsCommandHelper.invalidEditQuestionMessage(currentState)
                    )
            );
            return;
        }

        switch (currentState) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> bot.setToken(userResponse);
            case ASK_BOT_NAME -> bot.setName(userResponse);
            case ASK_WELCOME_MESSAGE -> bot.setWelcomeMessage(userResponse);
            case ASK_WORKING_HOURS -> {
                List<WorkingHours> workingHours = MessageExtractor.extractWorkingHours(userResponse);
                bot.setWorkingHours(workingHours);
            }
            case ASK_JOBS -> {
                List<Job> jobs = MessageExtractor.extractJobs(userResponse);
                bot.setJobs(jobs);
            }
            default -> log.info("Unknown state: {} in editing a bot field", currentState);
        }

        // TODO: show success command + add buttons to return to prev state

        bot.setCreationState(BotCreationState.COMPLETED);
        botApi.updateBot(bot);
        messageBatchProcessor.addMessage(
                MessageGenerator.createSimpleTextMessage(
                        message.getChatId(),
                        successMessage(currentState)
                )
        );
    }

    private String successMessage(BotCreationState state) {
        return switch (state) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> "✅ Bot token successfully changed!";
            case ASK_BOT_NAME -> "✅ Bot name changed successfully!";
            case ASK_WELCOME_MESSAGE -> "✅ Welcome message changed successfully!";
            case ASK_WORKING_HOURS -> "✅ Working hours are changed.";
            case ASK_JOBS -> "✅ Working durations are changed.";
            default -> "";
        };
    }
}

