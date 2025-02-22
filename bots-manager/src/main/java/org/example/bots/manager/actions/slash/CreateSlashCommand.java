package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.actions.helpers.CommonCommandHelper;
import org.example.bots.manager.utils.MessageExtractor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.controller.DynamicBotApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.telegram.components.validators.BotMessageValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateSlashCommand implements ISlashCommand {
    private final BotApi botApi;
    private final BusinessOwnerApi businessOwnerApi;
    private final DynamicBotApi dynamicBotApi;
    private final MessageBatchProcessor messageBatchProcessor;
    private final CommonCommandHelper commonCommandHelper;

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
            BotCreationState previousState = currentState.getPreviousState().get();
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
        AtomicReference<String> response = new AtomicReference<>("");

        currentState.getNextState().ifPresentOrElse(nextState -> {
            switch (currentState) {
                case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> setTokenAndUsername(bot, userInput);
                case ASK_BOT_NAME -> bot.setName(userInput);
                case ASK_WELCOME_MESSAGE -> bot.setWelcomeMessage(userInput);
                case ASK_WORKING_HOURS -> buildAndSaveWorkingHours(bot, userInput);
                case ASK_JOBS -> buildAndSaveJobs(bot, userInput);
                default -> {
                }
            }
            bot.setCreationState(nextState);

            Bot savedBot = botApi.updateBot(bot);
            if (nextState == BotCreationState.COMPLETED) {
                dynamicBotApi.registerBot(savedBot);
            }
            response.set(successMessage(currentState) + "\n\n" + nextState.getMessage());
        }, () -> {
            // If no next state, user has completed registration
//            botSessionService.finalizeBotSession(userId, botSession);
//            businessOwnerApi.addBot(userId, bot);
//            response.set("🎉 Your new bot has been created successfully!\nYou can now access it using the link from the first message.\n\n🙏 Thank you for creating new bot with us! Type any text to continue.");
        });
        return response.get();
    }

    private String successMessage(BotCreationState state) {
        return switch (state) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> "✅ Bot creation message is verified!";
            case ASK_BOT_NAME -> "✅ Bot name saved successfully!";
            case ASK_WELCOME_MESSAGE -> "✅ Welcome message saved successfully!";
            case ASK_WORKING_HOURS -> "✅ Working hours are saved.";
            case ASK_JOBS -> "✅ Working durations are saved.";
            default -> "";
        };
    }

    private void setTokenAndUsername(Bot bot, String userInput) {
        bot.setToken(BotMessageValidator.extractToken(userInput));
        bot.setUsername(BotMessageValidator.extractBotLink(userInput));
    }

    private void buildAndSaveWorkingHours(Bot bot, String workingHoursStr) {
        List<WorkingHours> workingHours = MessageExtractor.extractWorkingHours(workingHoursStr);
        bot.addWorkingHours(workingHours);
    }

    private void buildAndSaveJobs(Bot bot, String workingDurationsStr) {
        List<Job> jobs = MessageExtractor.extractJobs(workingDurationsStr);
        bot.addJobs(jobs);
    }
}

