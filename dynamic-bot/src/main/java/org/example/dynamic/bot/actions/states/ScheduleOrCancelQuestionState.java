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
                context.setState(callbackData.getFrom().getId().toString(), bot.getId(), scheduleState);
                scheduleState.handle(context, bot, message);
                return;
            } else if (Callback.CANCEL_APPOINTMENT.equals(data)) {
                CancelAppointmentsState cancelAppointmentsState = context.getCancelAppointmentsState();
                context.setState(callbackData.getFrom().getId().toString(), bot.getId(), cancelAppointmentsState);
                cancelAppointmentsState.handle(context, bot, message);
                return;
            }
        }
        createScheduleOrCancelButtons(chatId, bot, message);
    }

    private void createScheduleOrCancelButtons(String chatId, Bot bot, Message message) {
        String text = bot.getWelcomeMessage() + "\n\n" + "What would you like to do?";
        Client currentClient = clientApi.getClient(Long.parseLong(chatId));
        String[][] buttonConfigs = currentClient.getAppointments().isEmpty()
                ? new String[][]{{"📅 Schedule An Appointment:" + Callback.SCHEDULE_APPOINTMENT}}
                : new String[][]{
                {"📅 Schedule An Appointment:" + Callback.SCHEDULE_APPOINTMENT},
                {"❌ Cancel An Existing Appointment:" + Callback.CANCEL_APPOINTMENT}
        };
        messageBatchProcessor.addTextUpdate(
                MessageGenerator.createEditMessageWithMarkup(chatId, text, commonStateHelper.createInlineKeyboard(buttonConfigs), message.getMessageId())
        );
    }
}
