package org.example.telegram.bot.polling;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegram.bot.config.ConfigLoader;
import org.example.telegram.bot.services.manager.ManagerMessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@AllArgsConstructor
public class BotsManager extends TelegramLongPollingBot {

    private final DynamicBot dynamicBot;
    private final ManagerMessageService managerMessageService;
    private final ConfigLoader configLoader;

    @Override
    public String getBotUsername() {
        return configLoader.getUsername();
    }

    @Override
    public String getBotToken() {
        return configLoader.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (isAppointmentConfirmations(update)) {
//            handleAppointmentMessage(update); // adjust message to correct state
            // edit the markup correctly
//            SendMessage response = SendMessage.builder()
//                    .chatId(update.getCallbackQuery().getMessage().getChatId())
//                    .text("Confirmed");
//            dynamicBot.handleAppointmentResponse()
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            BotApiMethod<?> response = managerMessageService.processMessage(update);
            sendMessage(response);
        }
    }

    public void sendMessageToUser(SendMessage message) {
        sendMessage(message);
    }

    private boolean isAppointmentConfirmations(Update update) {
        boolean isAppointment = update.getCallbackQuery().getData().startsWith("confirmAppointment") ||
                update.getCallbackQuery().getData().startsWith("declineAppointment");
        return update.hasCallbackQuery() && isAppointment;
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

