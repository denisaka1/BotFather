package org.example.bots.manager.actions;

import lombok.RequiredArgsConstructor;
import org.example.client.api.controller.ClientApi;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AppointmentProcessor {

    private final ClientApi clientApi;

    public void processCallbackResponse(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.startsWith("confirmAppointment")) {
            // TODO: continue from here
            String appointmentId = callbackData.split("confirmAppointment")[1];
//            clientApi.updateAppointment()
//            botApi.
        }
        System.out.println(update);
        return;
    }
}
