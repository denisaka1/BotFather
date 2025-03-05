package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.actions.helpers.BotsCommandHelper;
import org.example.bots.manager.actions.helpers.CommonCommandHelper;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotsSlashCommand implements ISlashCommand {

    private final BusinessOwnerApi businessOwnerApi;
    private final MessageBatchProcessor messageBatchProcessor;
    private final BotsCommandHelper botsCommandHelper;
    private final CommonCommandHelper commonCommandHelper;

    public void execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!commonCommandHelper.botsExist(userId)) {
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

        for (Map.Entry<String, BiConsumer<CallbackQuery, String>> entry : botsCommandHelper.getCallbackHandlers().entrySet()) {
            String key = entry.getKey();

            if (callbackData.startsWith(key)) {
                String botId = callbackData.replace(key, "");
                entry.getValue().accept(update.getCallbackQuery(), botId);
                return;
            }
        }
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

        if (!botsCommandHelper.containsStateKey(currentState)) {
            log.info("Unknown state: {} in editing a bot field", currentState);
            return;
        }

        botsCommandHelper.acceptState(currentState, bot, message);
    }
}

