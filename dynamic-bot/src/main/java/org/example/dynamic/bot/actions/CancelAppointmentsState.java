package org.example.dynamic.bot.actions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.AppointmentsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class CancelAppointmentsState implements IDynamicBotState {
    private final ClientApi clientApi;

    @Override
    public BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
//        if (callbackData != null) {
//            return handleCallbackQuery(bot, message, callbackData, context);
//        }


        if (message.hasText() && !message.getFrom().getIsBot()) {
            return null;
        }

        return sendAppointmentsList(message.getChatId(), message, bot);
    }

//    private BotApiMethod<?> handleCallbackQuery(Bot bot, Message message, CallbackQuery callbackData, DynamicMessageService context) {
//    }

    private BotApiMethod<?> sendAppointmentsList(Long chatId, Message message, Bot bot) {
        List<Appointment> appointments = List.of(clientApi.findAppointments(chatId, bot.getId()));
        InlineKeyboardMarkup appointmentsKeyboard = AppointmentsGenerator.generateAppointmentsKeyboard(
                appointments, 0
        );
        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "Please select the appointment you want to cancel:",
                appointmentsKeyboard, message.getMessageId()
        );
    }
}
