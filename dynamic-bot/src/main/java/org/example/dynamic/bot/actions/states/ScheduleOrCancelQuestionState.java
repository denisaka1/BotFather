package org.example.dynamic.bot.actions.states;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.ClientApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.dynamic.bot.actions.helpers.CommonStateHelper;
import org.example.dynamic.bot.constants.Callback;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduleOrCancelQuestionState implements IDynamicBotState {
    private final ClientApi clientApi;
    private final CommonStateHelper commonStateHelper;
    private final MessageBatchProcessor messageBatchProcessor;

    @Override
    public void handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        String chatId = message.getChatId().toString();
        if (callbackData != null) {
            String data = callbackData.getData();
            if (Callback.SCHEDULE_APPOINTMENT.equals(data)) {
                ScheduleState scheduleState = context.getScheduleState();
                scheduleState.handle(context, bot, message);
            } else if (Callback.CANCEL_APPOINTMENT_BUTTON.equals(data)) {
                CancelAppointmentsState cancelAppointmentsState = context.getCancelAppointmentsState();
                cancelAppointmentsState.handle(context, bot, message);
            } else {
                createScheduleOrCancelButtons(chatId, bot, message, true);
            }
        } else {
            createScheduleOrCancelButtons(chatId, bot, message, false);
        }
    }

    private void createScheduleOrCancelButtons(String chatId, Bot bot, Message message, boolean shouldEdit) {
        String text = bot.getWelcomeMessage() + "\n\n" + "What would you like to do?";
        Client currentClient = clientApi.getClient(Long.parseLong(chatId));
        String[][] buttonConfigs = currentClient.getAppointments().isEmpty()
                ? new String[][]{{"📅 Schedule An Appointment:" + Callback.SCHEDULE_APPOINTMENT}}
                : new String[][]{
                {"📅 Schedule An Appointment:" + Callback.SCHEDULE_APPOINTMENT},
                {"❌ Cancel An Existing Appointment:" + Callback.CANCEL_APPOINTMENT_BUTTON}
        };
        if (shouldEdit) {
            messageBatchProcessor.addTextUpdate(
                    MessageGenerator.createEditMessageWithMarkup(chatId, text, commonStateHelper.createInlineKeyboard(buttonConfigs), message.getMessageId())
            );
        } else {
            messageBatchProcessor.addMessage(
                    MessageGenerator.createSendMessageWithMarkup(chatId, text, commonStateHelper.createInlineKeyboard(buttonConfigs))
            );
        }
    }
}
