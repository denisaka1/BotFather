package org.example.dynamic.bot.actions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BotsManagerApi;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.*;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduleState implements IDynamicBotState {
    private final BotsManagerApi botsManagerApi;
    private final BotApi botApi;
    private final ClientApi clientApi;

    @Override
    public BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        if (callbackData != null) {
            return handleCallbackQuery(bot, message, callbackData, context);
        }

        if (message.hasText() && !message.getFrom().getIsBot()) {
            return null;
        }

        return sendCalendar(message.getChatId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot);
    }

    private List<Job> fetchBotJobs(Bot bot) {
        return List.of(botApi.getJobs(bot.getId()));
    }

    private BotApiMethod<?> handleCallbackQuery(Bot bot, Message message, CallbackQuery callback, DynamicMessageService context) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String callbackData = callback.getData();

        if (callbackData.startsWith(Appointment.AppointmentCreationStep.DATE_SELECTED.name())) {
            return sendJobSelection(chatId, messageId, callbackData.split(":")[1], bot);
        }
        if (callbackData.startsWith(Appointment.AppointmentCreationStep.UPDATE_DATES.name())) {
            return updateCalendar(chatId, messageId, callbackData, bot.getWorkingHours());
        }
        if (callbackData.startsWith(Appointment.AppointmentCreationStep.JOB_SELECTED.name())) {
            return sendHourSelection(chatId, messageId, callbackData, bot);
        }
        if (callbackData.equals(Appointment.AppointmentCreationStep.BACK_TO_DATES.name())) {
            return sendCalendar(chatId, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot);
        }
        if (callbackData.startsWith(Appointment.AppointmentCreationStep.HOUR_SELECTED.name())) {
            return handleAppointmentCreation(chatId, messageId, bot, callbackData);
        }
        if (callbackData.startsWith(Appointment.AppointmentCreationStep.UPDATE_HOURS.name())) {
            return updateHourSelection(chatId, messageId, callbackData, bot);
        }
        if (callbackData.startsWith(Appointment.AppointmentCreationStep.BACK_TO_JOBS.name())) {
            return sendJobSelection(chatId, messageId, callbackData.split("@")[1], bot);
        }
        if (Appointment.AppointmentCreationStep.BACK_TO_MENU.name().equals(callbackData)) {
            ScheduleOrCancelQuestionState scheduleOrCancelQuestionState = context.getScheduleOrCancelQuestionState();
            context.setState(callback.getFrom().getId().toString(), bot.getId(), scheduleOrCancelQuestionState);
            return scheduleOrCancelQuestionState.handle(context, bot, message, callback);
        }
        return null;
    }

    private BotApiMethod<?> sendHourSelection(Long chatId, Integer messageId, String callbackData, Bot bot) {
        String[] parts = callbackData.split("@");
        String selectedDate = parts[2];
        String[] jobParts = parts[1].split(":");
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        Bot updatedBot = botApi.getBot(bot.getId());
        InlineKeyboardMarkup hourKeyboard = HourKeyboardGenerator.generateHoursKeyboard(
                selectedDate, null, updatedBot.getWorkingHours(), parts[1], updatedBot.getAppointments()
        );

        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "✅ You selected " + jobType + " (" + jobDuration + "h) at: " + selectedDate + "\n\n⏳ Please select a time:",
                hourKeyboard, messageId
        );
    }

    private BotApiMethod<?> sendCalendar(Long chatId, int year, int month, Message message, Bot bot) {
        Bot updatedBot = botApi.getBot(bot.getId());
        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, updatedBot.getWorkingHours());
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "📅 Please select a date:", calendar, message.getMessageId());
    }

    private BotApiMethod<?> updateCalendar(Long chatId, Integer messageId, String callbackData, List<WorkingHours> workingHours) {
        String[] parts = callbackData.split(":")[1].split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, workingHours);
        return MessageGenerator.createEditMessageReplyMarkup(chatId.toString(), messageId, calendar);
    }

    private BotApiMethod<?> updateHourSelection(Long chatId, Integer messageId, String callbackData, Bot bot) {
        String[] parts = callbackData.split("@");
        if (parts.length < 4) {
            return new SendMessage(chatId.toString(), "⚠ Invalid hour selection!");
        }
        String selectedDate = parts[1];
        String[] jobParts = parts[2].split(":");
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        String startHour = parts[3];
        Bot updatedBot = botApi.getBot(bot.getId());
        InlineKeyboardMarkup hourKeyboard = HourKeyboardGenerator.generateHoursKeyboard(
                selectedDate, startHour, updatedBot.getWorkingHours(), parts[2], updatedBot.getAppointments()
        );

        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "✅ You selected " + jobType + " (" + jobDuration + "h) at: " + selectedDate + "\n\n⏳ Please select a time:",
                hourKeyboard, messageId
        );
    }

    private BotApiMethod<?> sendJobSelection(Long chatId, Integer messageId, String dateSelected, Bot bot) {
        List<Job> jobs = fetchBotJobs(bot);
        InlineKeyboardMarkup jobKeyboard = JobKeyboardBuilder.createJobSelectionKeyboard(jobs, dateSelected);
        String dayOfWeek = LocalDate.parse(dateSelected, DateTimeFormatter.ofPattern("dd/MM/yyyy")).getDayOfWeek().name();
        String formattedDayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "📅 You selected: " + dateSelected + " (" + formattedDayOfWeek + ").\n\n🛠 Please choose a service:",
                jobKeyboard, messageId
        );
    }

    private void saveAppointment(String selectedDate, String selectedTime, Long chatId, Bot bot, String jobId, String jobType, String jobDuration) {
        Client client = clientApi.getClient(chatId);
        LocalDateTime selectedDateTime = LocalDateTime.of(
                LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                LocalTime.parse(selectedTime, DateTimeFormatter.ofPattern("HH:mm"))
        );
        Appointment appointment = Appointment.
                builder().
                appointmentDate(selectedDateTime).
                status(Appointment.AppointmentStatus.PENDING).
                build();
        Appointment savedAppointment = clientApi.createAppointment(appointment, client.getId(), bot.getId(), Long.parseLong(jobId));
        BusinessOwner botOwner = botApi.getOwner(bot.getId());
        String confirmationMessage = "Hi " + botOwner.getFirstName() +
                " 👋\nA new " + jobType + " appointment (" + jobDuration + "h) has been scheduled for " + selectedDate + " at " + selectedTime
                + " by " + client.getName() + ". Phone number: " + client.getPhoneNumber() + ".\nWhat would you like to do?";
        String[][] buttonConfig = {
                {"CONFIRM ✅:confirmAppointment" + savedAppointment.getId(), "DECLINE ❌:declineAppointment" + savedAppointment.getId()}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        SendMessage confirmMsg = MessageGenerator.createSendMessageWithMarkup(
                botOwner.getUserTelegramId().toString(), confirmationMessage,
                markup
        );
        botsManagerApi.sendMessage(confirmMsg);
    }

    private BotApiMethod<?> handleAppointmentCreation(Long chatId, Integer messageId, Bot bot, String callbackData) {
        String[] parts = callbackData.split("@");

        if (parts.length < 4) {
            return new SendMessage(chatId.toString(), "⚠ Invalid selection!");
        }

        String selectedDate = parts[1];
        String[] jobParts = parts[2].split(":");
        String jobId = jobParts[0];
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        String selectedTime = parts[3];
        saveAppointment(selectedDate, selectedTime, chatId, bot, jobId, jobType, jobDuration);
        String returnMessage = String.format(
                "✅ Your appointment for a %s (%s h) on %s at %s is waiting for confirmation.\nYou'll receive a confirmation message here shortly.",
                jobType, jobDuration, selectedDate, selectedTime
        );
        String[][] buttonConfig = {
                {"<< Back To Menu:" + Appointment.AppointmentCreationStep.BACK_TO_MENU.name()}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), returnMessage,
                markup, messageId
        );
    }
}
