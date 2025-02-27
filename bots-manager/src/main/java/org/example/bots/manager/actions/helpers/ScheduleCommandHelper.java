package org.example.bots.manager.actions.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.constants.Callback;
import org.example.data.layer.entities.Appointment;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleCommandHelper {
    public InlineKeyboardMarkup appointmentsList(List<Appointment> appointments, String date, String botId) {
        List<Appointment> upcomingAppointments = appointments.stream()
                .filter(appointment -> appointment.getStatus() != Appointment.AppointmentStatus.CANCELED && appointment.getAppointmentDate().isAfter(LocalDateTime.now()))
                .toList();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Appointment appointment : upcomingAppointments) {
            String appointmentDate = appointment.getAppointmentDate().toLocalDate().format(dateFormatter);
            String appointmentTime = appointment.getAppointmentDate().toLocalTime().format(timeFormatter);
            String jobType = appointment.getJob().getType();
            String jobDuration = appointment.getJob().getDuration() + "h";
            String buttonText = String.format("%s (%s): %s at %s, Status: %s",
                    jobType,
                    jobDuration,
                    appointmentDate,
                    appointmentTime,
                    appointment.getStatus());
            String callbackData = Callback.SELECT_APPOINTMENT + appointment.getId() + Callback.DELIMITER_SCHEDULE_STATE_DATES + date + Callback.DELIMITER_SCHEDULE_STATE_DATES + botId;
            InlineKeyboardButton appointmentButton = ButtonsGenerator.createButton(buttonText, callbackData);
            rows.add(List.of(appointmentButton));
        }
        InlineKeyboardButton backToDatesButton = ButtonsGenerator.createButton("<< Back To Dates", Callback.BACK_TO_DATES + botId);
        rows.add(List.of(backToDatesButton));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public InlineKeyboardMarkup appointmentOptions(Appointment.AppointmentStatus appointmentStatus, String appointmentId, String date, String botId) {
        boolean isPending = appointmentStatus == Appointment.AppointmentStatus.PENDING;
        String suffix = appointmentId + Callback.DELIMITER_SCHEDULE_STATE_DATES
                + date + Callback.DELIMITER_SCHEDULE_STATE_DATES + botId;
        String[][] buttonConfig = {
                isPending
                        ? new String[]{"CONFIRM ✅:" + Callback.CONFIRM_APPOINTMENT + suffix, "CANCEL ❌:" + Callback.CANCEL_APPOINTMENT + suffix}
                        : new String[]{"CANCEL ❌:" + Callback.CANCEL_APPOINTMENT + suffix},
                {"🧑‍💼 Display Client Details:" + Callback.DISPLAY_CLIENT_DETAILS + suffix},
                {"<< Back To Appointments:" + Callback.BACK_TO_APPOINTMENTS + " " + Callback.DELIMITER_SCHEDULE_STATE_DATES + botId + Callback.DELIMITER_SCHEDULE_STATE_DATES + date}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }
}
