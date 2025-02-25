package org.example.dynamic.bot.actions.states;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.WorkingHours;
import org.example.dynamic.bot.actions.helpers.ScheduleStateHelper;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.CalendarKeyboardGenerator;
import org.example.telegram.components.inline.keyboard.HourKeyboardGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduleState implements IDynamicBotState {
    private final BotApi botApi;
    private final ScheduleStateHelper scheduleStateHelper;
    private final MessageBatchProcessor messageBatchProcessor;

    @Override
    public void handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        if (message.hasText() && !message.getFrom().getIsBot()) return;
        if (callbackData != null) {
            handleCallbackQuery(bot, message, callbackData, context);
        } else {
            sendCalendar(message.getChatId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot);
        }
    }

    private void handleCallbackQuery(Bot bot, Message message, CallbackQuery callback, DynamicMessageService context) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String callbackData = callback.getData();

        if (callbackData.startsWith(Appointment.AppointmentCreationStep.DATE_SELECTED.name())) {
            sendJobSelection(chatId, messageId, scheduleStateHelper.parseJobData(callbackData)[1], bot);
        } else if (callbackData.startsWith(Appointment.AppointmentCreationStep.UPDATE_DATES.name())) {
            updateCalendar(chatId, messageId, callbackData, bot.getWorkingHours());
        } else if (callbackData.startsWith(Appointment.AppointmentCreationStep.JOB_SELECTED.name())) {
            sendHourSelection(chatId, messageId, callbackData, bot);
        } else if (callbackData.equals(Appointment.AppointmentCreationStep.BACK_TO_DATES.name())) {
            sendCalendar(chatId, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot);
        } else if (callbackData.startsWith(Appointment.AppointmentCreationStep.HOUR_SELECTED.name())) {
            handleAppointmentCreation(chatId, messageId, bot, callbackData);
        } else if (callbackData.startsWith(Appointment.AppointmentCreationStep.UPDATE_HOURS.name())) {
            updateHourSelection(chatId, messageId, callbackData, bot);
        } else if (Appointment.AppointmentCreationStep.BACK_TO_MENU.name().equals(callbackData)) {
            sendBackToMenu(context, bot, message, callback, callback.getFrom().getId().toString());
        } else if (callbackData.startsWith(Appointment.AppointmentCreationStep.BACK_TO_JOBS.name())) {
            sendJobSelection(chatId, messageId, scheduleStateHelper.parseCallbackData(callbackData)[1], bot);
        }
    }

    private void sendBackToMenu(DynamicMessageService context, Bot bot, Message message, CallbackQuery callback, String userTelegramId) {
        ScheduleOrCancelQuestionState scheduleOrCancelQuestionState = context.getScheduleOrCancelQuestionState();
        context.setState(userTelegramId, bot.getId(), scheduleOrCancelQuestionState);
        scheduleOrCancelQuestionState.handle(context, bot, message, callback);
    }

    private void sendHourSelection(Long chatId, Integer messageId, String callbackData, Bot bot) {
        String[] parts = scheduleStateHelper.parseCallbackData(callbackData);
        String selectedDate = parts[2];
        String[] jobParts = scheduleStateHelper.parseJobData(parts[1]);
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        Bot updatedBot = botApi.getBot(bot.getId());
        InlineKeyboardMarkup hourKeyboard = HourKeyboardGenerator.generateHoursKeyboard(
                selectedDate, null, updatedBot.getWorkingHours(), parts[1], updatedBot.getAppointments()
        );

        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "✅ You selected " + jobType + " (" + jobDuration + "h) at: " + selectedDate + "\n\n⏳ Please select a time:",
                hourKeyboard, messageId
        ));
    }

    private void sendCalendar(Long chatId, int year, int month, Message message, Bot bot) {
        Bot updatedBot = botApi.getBot(bot.getId());
        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, updatedBot.getWorkingHours(),
                Appointment.AppointmentCreationStep.DATE_SELECTED.name() + ":",
                Appointment.AppointmentCreationStep.UPDATE_DATES.name() + ":", "<< Back To Menu", "BACK");
        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "📅 Please select a date:", calendar, message.getMessageId()));
    }

    private void updateCalendar(Long chatId, Integer messageId, String callbackData, List<WorkingHours> workingHours) {
        String[] parts = scheduleStateHelper.parseJobData(callbackData)[1].split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, workingHours,
                Appointment.AppointmentCreationStep.DATE_SELECTED.name() + ":", Appointment.AppointmentCreationStep.UPDATE_DATES.name() + ":",
                "<< Back To Menu", "BACK");
        messageBatchProcessor.addButtonUpdate(MessageGenerator.createEditMessageReplyMarkup(chatId.toString(), messageId, calendar));
    }

    private void updateHourSelection(Long chatId, Integer messageId, String callbackData, Bot bot) {
        String[] parts = scheduleStateHelper.parseCallbackData(callbackData);
        if (parts.length < 4) {
            messageBatchProcessor.addMessage(new SendMessage(chatId.toString(), "⚠ Invalid hour selection!"));
            return;
        }
        String selectedDate = parts[1];
        String[] jobParts = scheduleStateHelper.parseJobData(parts[2]);
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        String startHour = parts[3];
        Bot updatedBot = botApi.getBot(bot.getId());
        InlineKeyboardMarkup hourKeyboard = HourKeyboardGenerator.generateHoursKeyboard(
                selectedDate, startHour, updatedBot.getWorkingHours(), parts[2], updatedBot.getAppointments()
        );

        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "✅ You selected " + jobType + " (" + jobDuration + "h) at: " + selectedDate + "\n\n⏳ Please select a time:",
                hourKeyboard, messageId
        ));
    }

    private void sendJobSelection(Long chatId, Integer messageId, String dateSelected, Bot bot) {
        if (scheduleStateHelper.hasReachedDailyLimit(chatId, bot.getId(), dateSelected)) {
            String[][] buttonConfig = {
                    {"<< Back To Dates:" + Appointment.AppointmentCreationStep.BACK_TO_DATES.name()}
            };
            List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup.setKeyboard(keyboard);
            messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                    chatId.toString(), "⚠️ You have already reached the maximum number of appointments on " + dateSelected + ". Please choose a different date.",
                    markup, messageId
            ));
        } else {
            messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                    chatId.toString(), "📅 You selected: " + dateSelected + " (" + scheduleStateHelper.getFormattedDayOfWeek(dateSelected) + ").\n\n🛠 Please choose a service:",
                    scheduleStateHelper.generateJobSelectionKeyboard(bot, dateSelected), messageId
            ));
        }
    }

    private void handleAppointmentCreation(Long chatId, Integer messageId, Bot bot, String callbackData) {
        String[] parts = scheduleStateHelper.parseCallbackData(callbackData);

        if (parts.length < 4) {
            messageBatchProcessor.addMessage(new SendMessage(chatId.toString(), "⚠ Invalid selection!"));
            return;
        }

        String selectedDate = parts[1];
        String[] jobParts = scheduleStateHelper.parseJobData(parts[2]);
        String jobId = jobParts[0];
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        String selectedTime = parts[3];
        scheduleStateHelper.saveAppointment(selectedDate, selectedTime, chatId, bot, jobId, jobType, jobDuration);
        String returnMessage = scheduleStateHelper.generateClientConfirmationMessage(jobType, jobDuration, selectedDate, selectedTime);
        String[][] buttonConfig = {
                {"<< Back To Menu:" + Appointment.AppointmentCreationStep.BACK_TO_MENU.name()}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        messageBatchProcessor.addDeleteMessage(MessageGenerator.deleteMessage(chatId.toString(), messageId));
        messageBatchProcessor.addMessage(MessageGenerator.createSimpleTextMessage(
                chatId, returnMessage
        ));

        messageBatchProcessor.addMessage(MessageGenerator.createSendMessageWithMarkup(
                chatId.toString(), "⬅ Back", markup
        ));
    }
}
