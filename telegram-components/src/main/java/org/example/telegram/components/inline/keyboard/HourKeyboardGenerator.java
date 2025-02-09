package org.example.telegram.components.inline.keyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HourKeyboardGenerator {
    public static InlineKeyboardMarkup generateHourKeyboard(String selectedDate, int startHour, int endHour, int workingHoursStart, int workingHoursEnd, int hourDisplayRange) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().plusMinutes(15 - (LocalTime.now().getMinute() % 15)); // Round up to the next 15-minute slot
        LocalDate selected = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        int adjustedStartHour = startHour;

        // If the selected date is today, don't show past hours
        if (selected.equals(today)) {
            adjustedStartHour = Math.max(startHour, now.getHour());
        }

        // Ensure we do not exceed working hours
        if (adjustedStartHour < workingHoursStart) adjustedStartHour = workingHoursStart;
        if (endHour > workingHoursEnd) endHour = workingHoursEnd;

        // Generate buttons for every 15 minutes within the range
        for (int hour = adjustedStartHour; hour < endHour; hour++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int minutes = 0; minutes < 60; minutes += 15) {
                if (selected.equals(today) && (hour < now.getHour() || (hour == now.getHour() && minutes < now.getMinute()))) {
                    continue; // Skip past times if today
                }
                String time = String.format("%02d:%02d", hour, minutes);
                row.add(InlineKeyboardButton.builder()
                        .text(time)
                        .callbackData("selectedTime:" + selectedDate + ":" + time)
                        .build());
            }
            if (!row.isEmpty()) {
                rows.add(row);
            }
        }

        // Navigation buttons (only show if within working hours)
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        // Show "Previous" button only if there's an earlier time available
        if (adjustedStartHour > workingHoursStart) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("⬅ Previous")
                    .callbackData("hour:" + selectedDate + ":" + Math.max(adjustedStartHour - hourDisplayRange, workingHoursStart) + ":" + adjustedStartHour)
                    .build());
        }

        // Show "Next" button only if there's a later time available
        if (endHour < workingHoursEnd) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("Next ➡")
                    .callbackData("hour:" + selectedDate + ":" + endHour + ":" + Math.min(endHour + hourDisplayRange, workingHoursEnd))
                    .build());
        }

        // Add navigation row only if at least one button exists
        if (!navigationRow.isEmpty()) {
            rows.add(navigationRow);
        }

        // Add "Back" button to return to the date selection
        List<InlineKeyboardButton> backRow = List.of(
                InlineKeyboardButton.builder()
                        .text("<< Back to Dates")
                        .callbackData("backToDates")
                        .build()
        );
        rows.add(backRow);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
