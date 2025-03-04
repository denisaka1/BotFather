package org.example.bots.manager.actions.helpers;

import lombok.Getter;
import org.example.bots.manager.utils.MessageExtractor;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.DynamicBotApi;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.telegram.components.validators.BotMessageValidator;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.example.data.layer.entities.BotCreationState.*;

@Getter
@Component
public class CreateSlashCommandHelper {
    private final Map<BotCreationState, BiConsumer<Bot, String>> stateActions = new EnumMap<>(BotCreationState.class);
    private final Map<BotCreationState, String> stateSuccessMessages = new EnumMap<>(BotCreationState.class);

    private final DynamicBotApi dynamicBotApi;
    private final BotApi botApi;

    public CreateSlashCommandHelper(DynamicBotApi dynamicBotApi, BotApi botApi) {
        this.dynamicBotApi = dynamicBotApi;
        this.botApi = botApi;

        stateActions.put(ASK_BOT_FATHER_BOT_CREATION_MESSAGE, this::setTokenAndUsername);
        stateActions.put(ASK_BOT_NAME, Bot::setName);
        stateActions.put(ASK_WELCOME_MESSAGE, Bot::setWelcomeMessage);
        stateActions.put(ASK_WORKING_HOURS, this::buildAndSaveWorkingHours);
        stateActions.put(ASK_JOBS, this::buildAndSaveJobs);

        stateSuccessMessages.put(ASK_BOT_FATHER_BOT_CREATION_MESSAGE, "✅ Bot creation message is verified!");
        stateSuccessMessages.put(ASK_BOT_NAME, "✅ Bot name saved successfully!");
        stateSuccessMessages.put(ASK_WELCOME_MESSAGE, "✅ Welcome message saved successfully!");
        stateSuccessMessages.put(ASK_WORKING_HOURS, "✅ Working hours are saved.");
        stateSuccessMessages.put(ASK_JOBS, "✅ Working durations are saved.");
    }

    public boolean containsKey(BotCreationState state) {
        return stateActions.containsKey(state);
    }

    public String acceptState(BotCreationState currentState, Bot bot, String userInput) {
        stateActions.get(currentState).accept(bot, userInput);
        BotCreationState nextState = currentState.getNextState();
        bot.setCreationState(nextState);

        Bot savedBot = botApi.updateBot(bot);
        if (nextState == BotCreationState.COMPLETED) {
            dynamicBotApi.registerBot(savedBot);
        }

        return stateSuccessMessages.get(currentState) + "\n\n" + nextState.getMessage();
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
