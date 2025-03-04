package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.actions.helpers.CommonCommandHelper;
import org.example.bots.manager.actions.helpers.CreateSlashCommandHelper;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateSlashCommand implements ISlashCommand {

    private final BotApi botApi;
    private final BusinessOwnerApi businessOwnerApi;
    private final MessageBatchProcessor messageBatchProcessor;
    private final CommonCommandHelper commonCommandHelper;
    private final CreateSlashCommandHelper createSlashCommandHelper;

    @Override
    public void execute(Message message) {
        Long userId = message.getFrom().getId();
//        botSession = botSessionService.getBotSession(chatId);

        Bot bot = businessOwnerApi.createBotIfNotPresent(userId);

        messageBatchProcessor.addMessage(
                SendMessage.builder()
                        .chatId(userId)
                        .text(SlashCommand.BACK_COMMAND_MESSAGE + bot.getCreationState().getMessage())
                        .build()
        );
    }

    public boolean isCompleted(Long userId) {
        return Arrays.stream(businessOwnerApi.getBots(userId))
                .allMatch(bot -> Objects.equals(bot.getCreationState(), BotCreationState.COMPLETED));
    }

    public String processUserResponse(Message message) {
        String userInput = message.getText();
        Long userId = message.getFrom().getId();
        Bot bot = businessOwnerApi.createBotIfNotPresent(userId);
        BotCreationState currentState = bot.getCreationState();

        if (Objects.equals(userInput, SlashCommand.BACK)) {
            BotCreationState previousState = currentState.getPrevState();
            if (previousState == currentState) {
                return currentState.getMessage();
            }
            bot.setCreationState(previousState);
            botApi.updateBot(bot);
            return SlashCommand.RETURNING_TO_PREVIOUS_MESSAGE + previousState.getMessage();
        } else if (!commonCommandHelper.isValidInput(currentState, userInput)) {
            return "❌ Invalid input!\n\n" + currentState.getMessage();
        }

        return processState(bot, userInput);
    }

    private String processState(Bot bot, String userInput) {
        BotCreationState currentState = bot.getCreationState();

        if (!createSlashCommandHelper.containsKey(currentState)) {
            return "Invalid bot creation state!";
        }

        return createSlashCommandHelper.acceptState(currentState, bot, userInput);
    }
}

