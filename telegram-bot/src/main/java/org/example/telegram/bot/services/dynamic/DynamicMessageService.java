package org.example.telegram.bot.services.dynamic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.telegram.bot.actions.dynamic.AuthState;
import org.example.telegram.bot.actions.dynamic.IDynamicBotState;
import org.example.telegram.bot.actions.dynamic.ScheduleOrCancelQuestionState;
import org.example.telegram.bot.actions.dynamic.ScheduleState;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class DynamicMessageService {
    private final Map<Long, IDynamicBotState> userStates = new HashMap<>();
    @Getter
    private final ApiRequestHelper apiRequestHelper;
    @Getter
    private final AuthState authState;
    @Getter
    private final ScheduleOrCancelQuestionState scheduleOrCancelQuestionState;
    @Getter
    private final ScheduleState scheduleState;

    public void setState(Long userId, IDynamicBotState newState) {
        userStates.put(userId, newState);
    }

    public BotApiMethod<?> processMessage(Bot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return handleTextMessage(update.getMessage(), bot);
        }
        if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update, bot);
        }
        return new SendMessage(getChatId(update), "I only understand text messages.");
    }

    private Client clientByTelegramId(Long userId) {
        try {
            return this.apiRequestHelper.get(
                    "http://localhost:8080/api/client/" + userId.toString(),
                    Client.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    private BotApiMethod<?> handleCallbackQuery(Update update, Bot bot) {
        Message message = update.getCallbackQuery().getMessage();
        Long userId = update.getCallbackQuery().getFrom().getId();
        IDynamicBotState currentState = getUserState(userId);
        if (currentState.isBackCommand(update.getCallbackQuery())) {
            IDynamicBotState previousState = currentState.getPreviousState(this);
            userStates.put(userId, previousState);
            return previousState.handle(this, bot, message, update.getCallbackQuery());
        }

        return currentState.handle(this, bot, message, update.getCallbackQuery());
    }

    private BotApiMethod<?> handleTextMessage(Message message, Bot bot) {
        Long userId = message.getFrom().getId();
        IDynamicBotState currentState = getUserState(userId);
        return currentState.handle(this, bot, message);
    }

    private IDynamicBotState getUserState(Long userId) {
        userStates.putIfAbsent(userId, determineInitialState(userId));
        return userStates.get(userId);
    }

    private IDynamicBotState determineInitialState(Long userId) {
        return (!userStates.containsKey(userId) && clientByTelegramId(userId) == null)
                ? authState
                : scheduleOrCancelQuestionState;
    }

    private String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return "Unknown Chat";
    }
}