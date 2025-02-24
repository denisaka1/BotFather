package org.example.dynamic.bot.actions.states;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.ClientApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.dynamic.bot.actions.helpers.CancelAppointmentsStateHelper;
import org.example.dynamic.bot.actions.helpers.CommonStateHelper;
import org.example.dynamic.bot.constants.Callback;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.AppointmentsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class CancelAppointmentsState implements IDynamicBotState {
    private final ClientApi clientApi;
    private final CommonStateHelper commonStateHelper;
    private final CancelAppointmentsStateHelper cancelAppointmentsStateHelper;
    private final MessageBatchProcessor messageBatchProcessor;

    @Override
    public void handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        if (message.hasText() && !message.getFrom().getIsBot()) return;
        if (callbackData != null) {
            handleCallbackQuery(bot, message, callbackData);
        } else {
            sendAppointmentsList(message.getChatId(), message, bot);
        }
    }

    private void handleCallbackQuery(Bot bot, Message message, CallbackQuery callbackData) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String data = callbackData.getData();
        if (data.startsWith(Appointment.AppointmentCreationStep.CANCEL_APPOINTMENT.name())) {
            sendAreYouSureMessage(chatId, messageId, data);
        } else if (data.startsWith(Callback.CANCEL_APPOINTMENT)) {
            sendCancellationMessage(chatId, messageId, data, bot);
        } else if (data.equals(Callback.NOT_CANCEL) || data.equals(Callback.BACK_TO_APPOINTMENTS)) {
            sendAppointmentsList(message.getChatId(), message, bot);
        }
    }

    private void sendCancellationMessage(Long chatId, Integer messageId, String appointmentData, Bot bot) {
        String[] appointmentParts = cancelAppointmentsStateHelper.parseAppointmentData(appointmentData);
        String appointmentId = appointmentParts[2];
        String appointmentDate = appointmentParts[3];
        String appointmentTime = appointmentParts[4];
        String jobType = appointmentParts[5];
        String jobDuration = appointmentParts[6];
        String returnMessage = String.format(
                "⚠ You have canceled your appointment on %s at %s.",
                appointmentDate, appointmentTime
        );
        String[][] buttonConfig = {
                {"<< Back To Appointments:" + Callback.BACK_TO_APPOINTMENTS}
        };
        clientApi.deleteAppointment(appointmentId, chatId.toString());
        cancelAppointmentsStateHelper.notifyManagerAboutCancelAppointment(jobDuration, jobType, appointmentDate, appointmentTime, chatId, bot.getId());
        messageBatchProcessor.addTextUpdate(
                MessageGenerator.createEditMessageWithMarkup(chatId.toString(), returnMessage, commonStateHelper.createInlineKeyboard(buttonConfig), messageId)
        );
    }

    private void sendAreYouSureMessage(Long chatId, Integer messageId, String appointmentData) {
        String[] appointmentParts = cancelAppointmentsStateHelper.parseAppointmentData(appointmentData);
        String appointmentDate = appointmentParts[2];
        String appointmentTime = appointmentParts[3];
        String text = "You are about to cancel your appointment at " + appointmentDate + ", " + appointmentTime + ".\nIs that correct?";
        messageBatchProcessor.addTextUpdate(
                MessageGenerator.createEditMessageWithMarkup(chatId.toString(), text, cancelAppointmentsStateHelper.generateConfirmationKeyboard(appointmentData), messageId)
        );
    }

    private void sendAppointmentsList(Long chatId, Message message, Bot bot) {
        List<Appointment> appointments = List.of(clientApi.findAppointments(chatId, bot.getId()));
        InlineKeyboardMarkup appointmentsKeyboard = AppointmentsGenerator.generateAppointmentsKeyboard(
                appointments, 0
        );
        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "❗️Please select the appointment you would like to cancel:",
                appointmentsKeyboard, message.getMessageId()
        ));
    }
}