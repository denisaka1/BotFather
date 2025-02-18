package org.example.telegram.components.inline.keyboard;

import org.example.data.layer.entities.Appointment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppointmentsGenerator {
    private static final int PAGE_SIZE = 10;
    private static final String DELIMITER = "@";

    public static InlineKeyboardMarkup generateAppointmentsKeyboard(List<Appointment> appointments, int page) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<Appointment> upcomingAppointments = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().isAfter(LocalDateTime.now()))
                .toList();
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, upcomingAppointments.size());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (int i = start; i < end; i++) {
            Appointment appointment = upcomingAppointments.get(i);
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

            String callbackData = String.format(Appointment.AppointmentCreationStep.CANCEL_APPOINTMENT.name() + DELIMITER +
                    appointment.getId() + DELIMITER + appointmentDate + DELIMITER + appointmentTime + DELIMITER + jobType + DELIMITER + jobDuration);

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(buttonText)
                    .callbackData(callbackData)
                    .build();

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }
        List<InlineKeyboardButton> navigationButtons = new ArrayList<>();
        if (page > 0) {
            navigationButtons.add(InlineKeyboardButton.builder()
                    .text("⬅ Previous")
                    .callbackData("PAGE_" + (page - 1))
                    .build());
        }
        if (end < upcomingAppointments.size()) {
            navigationButtons.add(InlineKeyboardButton.builder()
                    .text("Next ➡")
                    .callbackData("PAGE_" + (page + 1))
                    .build());
        }
        if (!navigationButtons.isEmpty()) {
            rows.add(navigationButtons);
        }

        rows.add(Collections.singletonList(InlineKeyboardButton.builder().text("<< Back To Menu").callbackData("BACK").build()));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
