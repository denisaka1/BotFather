package org.example.dynamic.bot.actions.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BotsManagerApi;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.BusinessOwner;
import org.example.data.layer.entities.Client;
import org.example.dynamic.bot.constants.Callback;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@Slf4j
@RequiredArgsConstructor
public class CancelAppointmentsStateHelper {
    private final CommonStateHelper commonStateHelper;
    private final BotApi botApi;
    private final ClientApi clientApi;
    private final BotsManagerApi botsManagerApi;

    public String[] parseAppointmentData(String appointmentData) {
        return appointmentData.split(Callback.DELIMITER);
    }

    public InlineKeyboardMarkup generateConfirmationKeyboard(String appointmentData) {
        String[][] buttonConfigs = {
                {"Yes, cancel the appointment:" + Callback.CANCEL_APPOINTMENT + Callback.DELIMITER + appointmentData},
                {"Nope, nevermind:" + Callback.NOT_CANCEL},
                {"<< Back to Appointments:" + Callback.BACK_TO_APPOINTMENTS}
        };
        return commonStateHelper.createInlineKeyboard(buttonConfigs);
    }

    public void notifyManagerAboutCancelAppointment(String jobDuration, String jobType, String appointmentDate, String appointmentTime, Long chatId, Long botId) {
        Client client = clientApi.getClient(chatId);
        BusinessOwner botOwner = botApi.getOwner(botId);
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
        botsManagerApi.sendMessage(commonStateHelper.createSendMessage(botOwner.getUserTelegramId(), cancelMsg));
    }
}


