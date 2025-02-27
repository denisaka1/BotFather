package org.example.dynamic.bot.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.client.api.controller.ClientApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.dynamic.bot.actions.states.*;
import org.example.dynamic.bot.constants.Callback;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class DynamicMessageService {
    @Getter
    private final AuthState authState;
    @Getter
    private final ScheduleOrCancelQuestionState scheduleOrCancelQuestionState;
    @Getter
    private final ScheduleState scheduleState;
    @Getter
    private final CancelAppointmentsState cancelAppointmentsState;
    private final ClientApi clientApi;
    private final MessageBatchProcessor messageBatchProcessor;

    public void processMessage(Bot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage(), bot);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update, bot);
        } else {
            messageBatchProcessor.addMessage(new SendMessage(getChatId(update), "I only understand text messages."));
        }
    }

    private Client clientByTelegramId(Long userId) {
        try {
            return clientApi.getClient(userId);
        } catch (Exception e) {
            return null;
        }
    }

    private void handleCallbackQuery(Update update, Bot bot) {
        Message message = update.getCallbackQuery().getMessage();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Client client = clientByTelegramId(userId);
        IDynamicBotState currentState = getCurrentUserStateFromCallback(client, update, userId);
        if (currentState.isBackCommand(update.getCallbackQuery())) {
            IDynamicBotState previousState = currentState.getPreviousState(this);
            previousState.handle(this, bot, message, update.getCallbackQuery());
        } else {
            currentState.handle(this, bot, message, update.getCallbackQuery());
        }
    }

    private void handleTextMessage(Message message, Bot bot) {
        Long userId = message.getFrom().getId();
        Client client = clientByTelegramId(userId);
        if (client == null) {
            createClient(userId);
            authState.handle(this, bot, message);
        } else if (client.getName() == null) {
            authState.handle(this, bot, message);
        } else {
            scheduleOrCancelQuestionState.handle(this, bot, message);
        }
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

    private boolean isCancelAppointmentState(String callbackData) {
        return Stream.of(Appointment.AppointmentCreationStep.CANCEL_APPOINTMENT.name(), Callback.CANCEL_APPOINTMENT,
                Callback.NOT_CANCEL, Callback.BACK_TO_APPOINTMENTS).anyMatch(callbackData::startsWith);
    }

    private boolean isScheduleOrCancelQuestionState(String callbackData) {
        return Stream.of(Callback.SCHEDULE_APPOINTMENT, Callback.CANCEL_APPOINTMENT_BUTTON).anyMatch(callbackData::startsWith);
    }

    private boolean isScheduleState(String callbackData) {
        return Stream.of(
                Appointment.AppointmentCreationStep.DATE_SELECTED.name(), Appointment.AppointmentCreationStep.UPDATE_DATES.name(),
                Appointment.AppointmentCreationStep.JOB_SELECTED.name(), Appointment.AppointmentCreationStep.BACK_TO_DATES.name(),
                Appointment.AppointmentCreationStep.HOUR_SELECTED.name(), Appointment.AppointmentCreationStep.UPDATE_HOURS.name(),
                Appointment.AppointmentCreationStep.BACK_TO_JOBS.name(), Appointment.AppointmentCreationStep.BACK_TO_MENU.name()
        ).anyMatch(callbackData::startsWith);
    }

    private IDynamicBotState convertCallbackDataToClass(String callbackData) {
        if (isCancelAppointmentState(callbackData)) {
            return cancelAppointmentsState;
        } else if (isScheduleOrCancelQuestionState(callbackData)) {
            return scheduleOrCancelQuestionState;
        } else if (isScheduleState(callbackData)) {
            return scheduleState;
        }
        return scheduleOrCancelQuestionState;
    }

    private void createClient(Long userId) {
        Client newClient = Client.builder()
                .telegramId(userId.toString())
                .build();
        clientApi.createClient(newClient);
    }

    private IDynamicBotState getCurrentUserStateFromCallback(Client client, Update update, Long userId) {
        if (client == null) {
            createClient(userId);
            return authState;
        }
        return convertCallbackDataToClass(update.getCallbackQuery().getData());
    }
}