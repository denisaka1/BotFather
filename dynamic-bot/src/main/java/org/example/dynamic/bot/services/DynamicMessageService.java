package org.example.dynamic.bot.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.data.layer.entities.ClientScheduleState;
import org.example.dynamic.bot.actions.AuthState;
import org.example.dynamic.bot.actions.IDynamicBotState;
import org.example.dynamic.bot.actions.ScheduleOrCancelQuestionState;
import org.example.dynamic.bot.actions.ScheduleState;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Service
@AllArgsConstructor
public class DynamicMessageService {
    @Getter
    private final AuthState authState;
    @Getter
    private final ScheduleOrCancelQuestionState scheduleOrCancelQuestionState;
    @Getter
    private final ScheduleState scheduleState;
    private final ClientApi clientApi;

    public void setState(String userTelegramId, Long botId, IDynamicBotState newState) {
        ClientScheduleState clientScheduleState = ClientScheduleState.builder()
                .botId(botId)
                .state(Client.AppointmentScheduleState.valueOf(newState.getClass().getSimpleName()))
                .build();
        clientApi.updateScheduleState(clientScheduleState, userTelegramId);
    }

    public IDynamicBotState getCurrentUserState(Client client, Long botId, Long userId) {
        if (client == null) {
            Client newClient = Client.builder()
                    .telegramId(userId.toString())
                    .build();
            clientApi.createClient(newClient);
            setState(userId.toString(), botId, authState);
            return authState;
        }
        Optional<ClientScheduleState> stateEntry = client.getScheduleStates().stream()
                .filter(state -> state.getBotId().equals(botId)) // Find the correct entry
                .findFirst();

        if (stateEntry.isPresent()) {
            String stateName = stateEntry.get().getState().name();
            return convertStateNameToClass(stateName);
        }
        return scheduleOrCancelQuestionState;
    }

    public IDynamicBotState convertStateNameToClass(String stateName) {
        return switch (stateName) {
            case "AuthState" -> authState;
            case "ScheduleState" -> scheduleState;
            default -> scheduleOrCancelQuestionState;
        };
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
            return clientApi.getClient(userId);
        } catch (Exception e) {
            return null;
        }
    }

    private BotApiMethod<?> handleCallbackQuery(Update update, Bot bot) {
        Message message = update.getCallbackQuery().getMessage();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Client client = clientByTelegramId(userId);
        IDynamicBotState currentState = getCurrentUserState(client, bot.getId(), userId);
        if (currentState.isBackCommand(update.getCallbackQuery())) {
            IDynamicBotState previousState = currentState.getPreviousState(this);
            setState(userId.toString(), bot.getId(), previousState);
            return previousState.handle(this, bot, message, update.getCallbackQuery());
        }

        return currentState.handle(this, bot, message, update.getCallbackQuery());
    }

    private BotApiMethod<?> handleTextMessage(Message message, Bot bot) {
        Long userId = message.getFrom().getId();
        Client client = clientByTelegramId(userId);
        IDynamicBotState currentState = getCurrentUserState(client, bot.getId(), userId);
        return currentState.handle(this, bot, message);
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