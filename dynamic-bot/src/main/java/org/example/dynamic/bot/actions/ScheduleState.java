package org.example.dynamic.bot.actions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BotsManagerApi;
import org.example.client.api.controller.ClientApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.*;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.example.data.layer.entities.Appointment.MAX_NUM_OF_APPOINTMENTS_PER_DAY;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduleState implements IDynamicBotState {
    private final BotsManagerApi botsManagerApi;
    private final BotApi botApi;
    private final MessageBatchProcessor messageBatchProcessor;
    private final ClientApi clientApi;

    @Override
    public void handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        if (message.hasText() && !message.getFrom().getIsBot()) return;
        if (callbackData != null) {
            handleCallbackQuery(bot, message, callbackData, context);
        } else {
            sendCalendar(message.getChatId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot);
        }
    }

    private List<Job> fetchBotJobs(Bot bot) {
        return List.of(botApi.getJobs(bot.getId()));
    }

    private void handleCallbackQuery(Bot bot, Message message, CallbackQuery callback, DynamicMessageService context) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String callbackData = callback.getData();

        if (callbackData.startsWith(Appointment.AppointmentCreationStep.DATE_SELECTED.name())) {
            sendJobSelection(chatId, messageId, callbackData.split(":")[1], bot);
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
        } else if (callbackData.startsWith(Appointment.AppointmentCreationStep.BACK_TO_JOBS.name())) {
            sendJobSelection(chatId, messageId, callbackData.split("@")[1], bot);
        } else if (Appointment.AppointmentCreationStep.BACK_TO_MENU.name().equals(callbackData)) {
            ScheduleOrCancelQuestionState scheduleOrCancelQuestionState = context.getScheduleOrCancelQuestionState();
            context.setState(callback.getFrom().getId().toString(), bot.getId(), scheduleOrCancelQuestionState);
            scheduleOrCancelQuestionState.handle(context, bot, message, callback);
        }
    }

    private void sendHourSelection(Long chatId, Integer messageId, String callbackData, Bot bot) {
        String[] parts = callbackData.split("@");
        String selectedDate = parts[2];
        String[] jobParts = parts[1].split(":");
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
        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, updatedBot.getWorkingHours());
        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "📅 Please select a date:", calendar, message.getMessageId()));
    }

    private void updateCalendar(Long chatId, Integer messageId, String callbackData, List<WorkingHours> workingHours) {
        String[] parts = callbackData.split(":")[1].split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, workingHours);
        messageBatchProcessor.addButtonUpdate(MessageGenerator.createEditMessageReplyMarkup(chatId.toString(), messageId, calendar));
    }

    private void updateHourSelection(Long chatId, Integer messageId, String callbackData, Bot bot) {
        String[] parts = callbackData.split("@");
        if (parts.length < 4) {
            messageBatchProcessor.addMessage(new SendMessage(chatId.toString(), "⚠ Invalid hour selection!"));
            return;
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

        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "✅ You selected " + jobType + " (" + jobDuration + "h) at: " + selectedDate + "\n\n⏳ Please select a time:",
                hourKeyboard, messageId
        ));
    }

    private void sendJobSelection(Long chatId, Integer messageId, String dateSelected, Bot bot) {
        if (hasReachedDailyAppointmentLimit(chatId, bot.getId(), dateSelected)) {
            String[][] buttonConfig = {
                    {"<< Back To Dates:" + Appointment.AppointmentCreationStep.BACK_TO_DATES.name()}
            };
            List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup.setKeyboard(keyboard);
            SendMessage confirmMsg = MessageGenerator.createSendMessageWithMarkup(
                    chatId.toString(), "⚠️ You have already reached the maximum number of appointments on " + dateSelected + ". Please choose a different date.",
                    markup
            );
            messageBatchProcessor.addMessage(confirmMsg);
        } else {
            List<Job> jobs = fetchBotJobs(bot);
            InlineKeyboardMarkup jobKeyboard = JobKeyboardBuilder.createJobSelectionKeyboard(jobs, dateSelected);
            String dayOfWeek = LocalDate.parse(dateSelected, DateTimeFormatter.ofPattern("dd/MM/yyyy")).getDayOfWeek().name();
            String formattedDayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
            messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                    chatId.toString(), "📅 You selected: " + dateSelected + " (" + formattedDayOfWeek + ").\n\n🛠 Please choose a service:",
                    jobKeyboard, messageId
            ));
        }
    }

    private Boolean hasReachedDailyAppointmentLimit(Long chatId, Long botId, String dateSelected) {
        List<Appointment> appointments = Optional.ofNullable(clientApi.findAppointmentsByDate(chatId, botId, dateSelected))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
        int appointmentsCount = appointments.isEmpty() ? 0 : appointments.size();
        return appointmentsCount >= MAX_NUM_OF_APPOINTMENTS_PER_DAY;
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

    private void handleAppointmentCreation(Long chatId, Integer messageId, Bot bot, String callbackData) {
        String[] parts = callbackData.split("@");

        if (parts.length < 4) {
            messageBatchProcessor.addMessage(new SendMessage(chatId.toString(), "⚠ Invalid selection!"));
            return;
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
        messageBatchProcessor.addDeleteMessage(MessageGenerator.deleteMessage(chatId.toString(), messageId));
        messageBatchProcessor.addMessage(MessageGenerator.createSimpleTextMessage(
                chatId, returnMessage
        ));

        messageBatchProcessor.addMessage(MessageGenerator.createSendMessageWithMarkup(
                chatId.toString(), "⬅ Back", markup
        ));
    }
}
