package org.example.bots.manager.actions;

import lombok.RequiredArgsConstructor;
import org.example.client.api.controller.AppointmentApi;
import org.example.client.api.controller.DynamicBotApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.constants.AppointmentConst;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AppointmentProcessor {

    private final AppointmentApi appointmentApi;
    private final DynamicBotApi dynamicBotApi;
    private final MessageBatchProcessor messageBatchProcessor;

    public void processCallbackResponse(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Message message = update.getCallbackQuery().getMessage();

        String responseToOwner;

        if (callbackData.startsWith(AppointmentConst.CONFIRM_APPOINTMENT)) {
            String appointmentId = callbackData.split(AppointmentConst.CONFIRM_APPOINTMENT)[1];
            responseToOwner = handleAcceptedAppointment(appointmentId);
        } else { // decline
            String appointmentId = callbackData.split(AppointmentConst.DECLINE_APPOINTMENT)[1];
            responseToOwner = handleDeclinedAppointment(appointmentId);
        }

        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        // adjusts the text
        messageBatchProcessor.addTextUpdate(
                MessageGenerator.createEditMessage(
                        chatId,
                        responseToOwner,
                        messageId
                )
        );
        messageBatchProcessor.addDeleteMessage(
                MessageGenerator.deleteMessage(
                        chatId,
                        messageId
                )
        );
    }

    private String handleAcceptedAppointment(String appointmentId) {
        Appointment appointment = appointmentApi.getAppointment(appointmentId);
        Client client = appointmentApi.getClient(appointment.getId());

        appointment.setStatus(Appointment.AppointmentStatus.APPROVED);
        appointmentApi.updateAppointment(appointment);

        dynamicBotApi.sendMessage(
                MessageGenerator.createSimpleTextMessage(
                        client.getTelegramId(),
                        "✅ Your appointment has been approved!"
                )
        );

        return "Appointment approved";
    }

    private String handleDeclinedAppointment(String appointmentId) {
        Appointment appointment = appointmentApi.getAppointment(appointmentId);
        Client client = appointmentApi.getClient(appointment.getId());

        appointment.setStatus(Appointment.AppointmentStatus.CANCELED);
        appointmentApi.updateAppointment(appointment);

        dynamicBotApi.sendMessage(
                MessageGenerator.createSimpleTextMessage(
                        client.getTelegramId(),
                        "❌ Your appointment has been declined!"
                )
        );

        return "Appointment declined!";
    }
}
