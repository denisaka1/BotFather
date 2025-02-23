package org.example.dynamic.bot.actions.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BotsManagerApi;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.*;
import org.example.dynamic.bot.constants.Callback;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.JobKeyboardBuilder;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.example.data.layer.entities.Appointment.MAX_NUM_OF_APPOINTMENTS_PER_DAY;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleStateHelper {
    private final ClientApi clientApi;
    private final BotApi botApi;
    private final BotsManagerApi botsManagerApi;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public String[] parseCallbackData(String callbackData) {
        return callbackData.split(Callback.DELIMITER);
    }

    public String[] parseJobData(String jobData) {
        return jobData.split(Callback.DATE_DELIMITER);
    }

    public InlineKeyboardMarkup generateJobSelectionKeyboard(Bot bot, String dateSelected) {
        List<Job> jobs = List.of(botApi.getJobs(bot.getId()));
        return JobKeyboardBuilder.createJobSelectionKeyboard(jobs, dateSelected);
    }

    public boolean hasReachedDailyLimit(Long chatId, Long botId, String dateSelected) {
        List<Appointment> appointments = Optional.ofNullable(clientApi.findAppointmentsByDate(chatId, botId, dateSelected))
                .map(List::of)
                .orElse(List.of());

        return appointments.size() >= MAX_NUM_OF_APPOINTMENTS_PER_DAY;
    }

    public String getFormattedDayOfWeek(String dateSelected) {
        String dayOfWeek = LocalDate.parse(dateSelected, DATE_FORMATTER).getDayOfWeek().name();
        return dayOfWeek.charAt(0) + dayOfWeek.substring(1).toLowerCase();
    }

    public LocalDateTime parseDateTime(String date, String time) {
        return LocalDateTime.of(LocalDate.parse(date, DATE_FORMATTER), LocalTime.parse(time, TIME_FORMATTER));
    }

    public String generateAppointmentNotification(Client client, String selectedDate, String selectedTime, String jobType, String jobDuration, BusinessOwner botOwner) {
        return String.format("Hi %s 👋\n📅 A new %s appointment (%s hours) has been scheduled for %s at %s by %s.\n☎ Phone: %s\n✉ Email: %s",
                botOwner.getFirstName(), jobType, jobDuration, selectedDate, selectedTime, client.getName(), client.getPhoneNumber(), client.getEmail());
    }

    public String generateClientConfirmationMessage(String jobType, String jobDuration, String selectedDate, String selectedTime) {
        return String.format("✅ Your appointment for a %s (%s hours) on %s at %s is waiting for confirmation. You will receive a confirmation message here shortly.",
                jobType, jobDuration, selectedDate, selectedTime);
    }

    public void saveAppointment(String selectedDate, String selectedTime, Long chatId, Bot bot, String jobId, String jobType, String jobDuration) {
        Client client = clientApi.getClient(chatId);
        Appointment appointment = Appointment.
                builder().
                appointmentDate(parseDateTime(selectedDate, selectedTime)).
                status(Appointment.AppointmentStatus.PENDING).
                build();
        Appointment savedAppointment = clientApi.createAppointment(appointment, client.getId(), bot.getId(), Long.parseLong(jobId));
        BusinessOwner botOwner = botApi.getOwner(bot.getId());
        String confirmationMessage = generateAppointmentNotification(client, selectedDate, selectedTime, jobType, jobDuration, botOwner);
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


}
