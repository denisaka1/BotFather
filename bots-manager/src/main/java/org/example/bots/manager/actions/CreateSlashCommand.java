package org.example.bots.manager.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.utils.MessageExtractor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.controller.DynamicBotApi;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.telegram.components.validators.BotMessageValidator;
import org.example.telegram.components.validators.StringValidator;
import org.example.telegram.components.validators.WorkingDurationsValidator;
import org.example.telegram.components.validators.WorkingHoursValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

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
    private Bot bot;

    @Override
    public SendMessage execute(Message message) {
        Long userId = message.getFrom().getId();
//        botSession = botSessionService.getBotSession(chatId);

        bot = businessOwnerApi.createBotIfNotPresent(userId);

        return SendMessage.builder()
                .chatId(userId)
                .text(bot.getCreationState().getMessage())
                .build();
    }

    public boolean isCompleted() {
        return bot.getCreationState().isCompleted();
    }

    public String processUserResponse(Message message) {
//        BotCreationState currentState = botSession.getCreationState();
        String userInput = message.getText();
        BotCreationState currentState = bot.getCreationState();

        if (Objects.equals(userInput, SlashCommand.BACK)) {
            BotCreationState previousState = currentState.getPreviousState().get();
            if (previousState == currentState) {
                return currentState.getMessage();
            }
            bot.setCreationState(previousState);
            botApi.updateBot(bot);
            return SlashCommand.RETURNING_TO_PREVIOUS_MESSAGE + previousState.getMessage();
        } else if (!isValidInput(currentState, userInput)) {
            return "❌ Invalid input!\n\n" + currentState.getMessage();
        }
//            return processPreviousState(currentState, userInput);

        return processState(currentState, userInput);
    }

    private String processState(BotCreationState currentState, String userInput) {
        AtomicReference<String> response = new AtomicReference<>("");
        currentState.getNextState().ifPresentOrElse(nextState -> {
            switch (currentState) {
                case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> setTokenAndUsername(userInput);
                case ASK_BOT_NAME -> bot.setName(userInput);
                case ASK_WELCOME_MESSAGE -> bot.setWelcomeMessage(userInput);
                case ASK_WORKING_HOURS -> buildAndSaveWorkingHours(userInput);
                case ASK_JOBS -> buildAndSaveJobs(userInput);
                default -> {
                }
            }
            bot.setCreationState(nextState);
            bot = botApi.updateBot(bot);
            if (nextState == BotCreationState.COMPLETED) {
                dynamicBotApi.registerBot(bot);
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

    private boolean isValidInput(BotCreationState state, String userMessage) {
        return switch (state) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> new BotMessageValidator().validate(userMessage);
            case ASK_BOT_NAME, ASK_WELCOME_MESSAGE -> new StringValidator().validate(userMessage);
            case ASK_WORKING_HOURS -> new WorkingHoursValidator().validate(userMessage);
            case ASK_JOBS -> new WorkingDurationsValidator().validate(userMessage);
            default -> true;
        };
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

    private void setTokenAndUsername(String userInput) {
        bot.setToken(BotMessageValidator.extractToken(userInput));
        bot.setUsername(BotMessageValidator.extractBotLink(userInput));
    }

    private void buildAndSaveWorkingHours(String workingHoursStr) {
        List<WorkingHours> workingHours = MessageExtractor.extractWorkingHours(workingHoursStr);
        for (WorkingHours workingHour : workingHours) {
            botApi.addWorkingHours(bot.getId(), workingHour);
        }
    }

    private void buildAndSaveJobs(String workingDurationsStr) {
        List<Job> jobs = MessageExtractor.extractJobs(workingDurationsStr);
        for (Job job : jobs) {
            botApi.addJob(bot.getId(), job);
        }
    }
}

