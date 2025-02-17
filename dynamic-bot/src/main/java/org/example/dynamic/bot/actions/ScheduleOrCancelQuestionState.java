package org.example.dynamic.bot.actions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduleOrCancelQuestionState implements IDynamicBotState {
    private final ClientApi clientApi;

    @Override
    public BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        String chatId = message.getChatId().toString();
        if (callbackData != null) {
            String data = callbackData.getData();
            if ("SCHEDULE".equals(data)) {
                ScheduleState scheduleState = context.getScheduleState();
                context.setState(callbackData.getFrom().getId().toString(), bot.getId(), scheduleState);
                return scheduleState.handle(context, bot, message);
            } else if ("CANCEL".equals(data)) {
                CancelAppointmentsState cancelAppointmentsState = context.getCancelAppointmentsState();
                context.setState(callbackData.getFrom().getId().toString(), bot.getId(), cancelAppointmentsState);
                return cancelAppointmentsState.handle(context, bot, message);
            } else if ("BACK".equals(data)) {
                return createScheduleOrCancelButtons(chatId, bot, message, true);
            } else if (Appointment.AppointmentCreationStep.BACK_TO_MENU.name().equals(data)) {
                return null;
            }
        }
        return createScheduleOrCancelButtons(chatId, bot, message, false);
    }

    private BotApiMethod<?> createScheduleOrCancelButtons(String chatId, Bot bot, Message message, boolean isBack) {
        String text = bot.getWelcomeMessage() + "\n\n" + "What would you like to do?";
        Client currentClient = clientApi.getClient(Long.parseLong(chatId));
        // Create inline keyboard with two rows
        String[][] buttonConfigs = currentClient.getAppointments().isEmpty()
                ? new String[][]{{"📅 Schedule An Appointment:SCHEDULE"}}
                : new String[][]{
                {"📅 Schedule An Appointment:SCHEDULE"},
                {"❌ Cancel An Existing Appointment:CANCEL"}
        };

        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfigs);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        if (isBack) {
            return MessageGenerator.createEditMessageWithMarkup(chatId, text, markup, message.getMessageId());
        } else {
            return MessageGenerator.createSendMessageWithMarkup(chatId, text, markup);
        }
    }
}
