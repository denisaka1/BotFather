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
        } else if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update);
        }
        return new SendMessage(update.getMessage().getChatId().toString(), "I only understand text messages.");
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

    private SendMessage handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if ("SCHEDULE".equals(callbackData)) {
            return new SendMessage(chatId.toString(), "You've selected: Schedule an appointment.");
        } else if ("EDIT".equals(callbackData)) {
            return new SendMessage(chatId.toString(), "You've selected: Delete an existing appointment.");
        }
        return new SendMessage(chatId.toString(), "I only understand text messages.");
    }

    private SendMessage handleTextMessage(Message message, Bot bot) {
        Long userId = message.getFrom().getId();
        Client currentClient = clientByTelegramId(userId);
        if (currentClient == null && !userStates.containsKey(userId)) {
            userStates.put(userId, new AuthState(apiRequestHelper, bot));
        }
        userStates.putIfAbsent(userId, new ScheduleOrCancelQuestionState());
        DynamicBotState currentState = userStates.get(userId);
        if (currentState.isBackCommand(message)) {
            userStates.put(userId, currentState.getPreviousState(this));
            return new SendMessage(message.getChatId().toString(), "Going back...");
        }
        return currentState.handle(this, bot, message);
    }
}