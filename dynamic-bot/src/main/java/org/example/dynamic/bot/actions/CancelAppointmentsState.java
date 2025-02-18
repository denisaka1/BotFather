package org.example.dynamic.bot.actions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BotsManagerApi;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BusinessOwner;
import org.example.data.layer.entities.Client;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.AppointmentsGenerator;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class CancelAppointmentsState implements IDynamicBotState {
    private final ClientApi clientApi;
    private final BotsManagerApi botsManagerApi;
    private final BotApi botApi;
    private static final String DELIMITER = "@";

    @Override
    public BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        if (callbackData != null) {
            return handleCallbackQuery(bot, message, callbackData, context);
        }
        if (message.hasText() && !message.getFrom().getIsBot()) return null;

        return sendAppointmentsList(message.getChatId(), message, bot);
    }

    private BotApiMethod<?> handleCallbackQuery(Bot bot, Message message, CallbackQuery callbackData, DynamicMessageService context) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String data = callbackData.getData();
        if (data.startsWith(Appointment.AppointmentCreationStep.CANCEL_APPOINTMENT.name())) {
            return sendAreYouSureMessage(chatId, messageId, data);
        }
        if (data.startsWith("Yes")) {
            return sendCancellationMessage(chatId, messageId, data, bot);
        }
        if (data.equals("No") || data.equals("BackToAppointments")) {
            return sendAppointmentsList(message.getChatId(), message, bot);
        }
        return null;
    }

    private BotApiMethod<?> sendCancellationMessage(Long chatId, Integer messageId, String appointmentData, Bot bot) {
        String[] appointmentParts = appointmentData.split(DELIMITER);
        String appointmentId = appointmentParts[2];
        String appointmentDate = appointmentParts[3];
        String appointmentTime = appointmentParts[4];
        String jobType = appointmentParts[5];
        String jobDuration = appointmentParts[6];
        String returnMessage = String.format(
                "You have canceled your appointment on %s at %s.",
                appointmentDate, appointmentTime
        );
        String[][] buttonConfig = {
                {"<< Back To Appointments:BackToAppointments"}
        };
        // cancel the appointment on the server
        clientApi.deleteAppointment(appointmentId, chatId.toString());
        // send a notice to the bots manager
        Client client = clientApi.getClient(chatId);
        BusinessOwner botOwner = botApi.getOwner(bot.getId());
        String cancelMsg = String.format("\uD83D\uDCE2 The %s (%s) appointment scheduled for %s at %s has been canceled by the " +
                        "client %s (Phone: %s, Email: %s).",
                jobType,
                jobDuration,
                appointmentDate,
                appointmentTime,
                client.getName(),
                client.getPhoneNumber(),
                client.getEmail()
        );
        SendMessage msg = new SendMessage(botOwner.getUserTelegramId().toString(), cancelMsg
        );
        botsManagerApi.sendMessage(msg);
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), returnMessage, markup, messageId);
    }

    private BotApiMethod<?> sendAreYouSureMessage(Long chatId, Integer messageId, String appointmentData) {
        String[] appointmentParts = appointmentData.split(DELIMITER);
        String appointmentDate = appointmentParts[2];
        String appointmentTime = appointmentParts[3];
        String[][] buttonConfigs = {
                {"Yes, cancel the appointment:Yes@" + appointmentData},
                {"Nope, nevermind:No"},
                {"<< Back to Appointments:BackToAppointments"}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfigs);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        String text = "You are about to cancel your appointment at " + appointmentDate + ", " + appointmentTime + ".\nIs that correct?";
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), text, markup, messageId);
    }

    private BotApiMethod<?> sendAppointmentsList(Long chatId, Message message, Bot bot) {
        List<Appointment> appointments = List.of(clientApi.findAppointments(chatId, bot.getId()));
        InlineKeyboardMarkup appointmentsKeyboard = AppointmentsGenerator.generateAppointmentsKeyboard(
                appointments, 0
        );
        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "❗️Please select the appointment you would like to cancel:",
                appointmentsKeyboard, message.getMessageId()
        );
    }
}
