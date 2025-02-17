package org.example.telegram.components.inline.keyboard;

import org.example.data.layer.entities.Appointment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsGenerator {
    private static final int PAGE_SIZE = 10;

    public static InlineKeyboardMarkup generateAppointmentsKeyboard(List<Appointment> appointments, int page) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Calculate the start and end indices for the current page
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, appointments.size());

        // Create buttons for each appointment on the current page
        for (int i = start; i < end; i++) {
            Appointment appointment = appointments.get(i);
            String buttonText = String.format("Appointment #%d: %s %s, %s",
                    appointment.getId(),
                    appointment.getAppointmentDate().toLocalDate(),
                    appointment.getAppointmentDate().toLocalTime(),
                    appointment.getStatus());

            String callbackData = String.format("APPOINTMENT_%d", appointment.getId());

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(buttonText)
                    .callbackData(callbackData)
                    .build();

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        // Add navigation buttons if needed
        List<InlineKeyboardButton> navigationButtons = new ArrayList<>();
        if (page > 0) {
            navigationButtons.add(InlineKeyboardButton.builder()
                    .text("⬅ Previous")
                    .callbackData("PAGE_" + (page - 1))
                    .build());
        }
        if (end < appointments.size()) {
            navigationButtons.add(InlineKeyboardButton.builder()
                    .text("Next ➡")
                    .callbackData("PAGE_" + (page + 1))
                    .build());
        }
        if (!navigationButtons.isEmpty()) {
            rows.add(navigationButtons);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
