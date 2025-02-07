package org.example.botfather.telegrambot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.entities.Client;
import org.example.botfather.telegrambot.dynamicbotstates.AuthState;
import org.example.botfather.telegrambot.dynamicbotstates.DynamicBotState;
import org.example.botfather.telegrambot.dynamicbotstates.ScheduleOrCancelQuestionState;
import org.example.botfather.utils.ApiRequestHelper;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class DynamicBotsMessageHandler {
    private final Map<Long, DynamicBotState> userStates = new HashMap<>();
    @Getter
    private final ApiRequestHelper apiRequestHelper;

    public void setState(Long userId, DynamicBotState newState) {
        userStates.put(userId, newState);
    }

    public SendMessage processMessage(Bot bot, Update update) {
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

    private SendMessage handleCallbackQuery(Update update, Bot bot) {
        String callbackData = update.getCallbackQuery().getData();
        Message message = update.getCallbackQuery().getMessage();
        Long userId = update.getCallbackQuery().getFrom().getId();

        return getUserState(userId, bot).handle(this, bot, message, callbackData);
    }

    private SendMessage handleTextMessage(Message message, Bot bot) {
        Long userId = message.getFrom().getId();
        DynamicBotState currentState = getUserState(userId, bot);

        if (currentState.isBackCommand(message)) {
            userStates.put(userId, currentState.getPreviousState(this));
            return new SendMessage(message.getChatId().toString(), "Going back...");
        }
        return currentState.handle(this, bot, message);
    }

    private DynamicBotState getUserState(Long userId, Bot bot) {
        userStates.putIfAbsent(userId, determineInitialState(userId, bot));
        return userStates.get(userId);
    }

    private DynamicBotState determineInitialState(Long userId, Bot bot) {
        return (!userStates.containsKey(userId) && clientByTelegramId(userId) == null)
                ? new AuthState(apiRequestHelper, bot)
                : new ScheduleOrCancelQuestionState();
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