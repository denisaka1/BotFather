package org.example.telegram.components.inline.keyboard;
import org.example.data.layer.entities.TimeRange;
import org.example.data.layer.entities.WorkingHours;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HourKeyboardGenerator {
    private static final int HOUR_DISPLAY_RANGE = 4;

    public static InlineKeyboardMarkup generateHourKeyboard(String selectedDate, String startHour, List<WorkingHours> workingHours) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().plusMinutes(15 - (LocalTime.now().getMinute() % 15)); // Round up to the next 15-minute slot
        LocalDate selected = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String day = selected.getDayOfWeek().name();
        List<String> availableHoursList = availableHours(workingHours, day);

        // Parse working hours start and end times from strings to LocalTime
        LocalTime parsedWorkingHoursStart = selected.equals(today) ? now : LocalTime.parse(availableHoursList.get(0).split("-")[0], DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime parsedWorkingHoursEnd = LocalTime.parse(availableHoursList.get(availableHoursList.size() - 1).split("-")[1], DateTimeFormatter.ofPattern("HH:mm"));

        // Parse startHour and endHour from strings to LocalTime
        LocalTime parsedStartHour = (startHour != null)
                ? LocalTime.parse(startHour, DateTimeFormatter.ofPattern("HH:mm"))
                : parsedWorkingHoursStart;

        LocalTime parsedEndHour = parsedStartHour.plusHours(HOUR_DISPLAY_RANGE);

        // Adjust startHour based on selected time
        LocalTime adjustedStartHour = parsedStartHour;

        // If the selected date is today, don't show past hours
        if (selected.equals(today)) {
            parsedWorkingHoursStart = now;
            adjustedStartHour = LocalTime.of(Math.max(parsedStartHour.getHour(), now.getHour()), 0); // Adjust to current hour if today
        }

        // Ensure we do not exceed working hours
        if (adjustedStartHour.isBefore(parsedWorkingHoursStart)) adjustedStartHour = parsedWorkingHoursStart;
        if (parsedEndHour.isAfter(parsedWorkingHoursEnd)) parsedEndHour = parsedWorkingHoursEnd;

        // Generate buttons for every 15 minutes within the range
        for (int hour = adjustedStartHour.getHour(); hour < parsedEndHour.getHour(); hour++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int minutes = 0; minutes < 60; minutes += 15) {
                if (selected.equals(today) && (hour + 1 < now.getHour() || (hour == now.getHour() && minutes < now.getMinute()))) {
                    continue; // Skip past times if today
                }
                String time = String.format("%02d:%02d", hour, minutes);

                if (!isBetween(availableHoursList, time)) continue;
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
        if (adjustedStartHour.isAfter(parsedWorkingHoursStart)) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("⬅ Previous")
                    .callbackData("hour:" + selectedDate + ":" + Math.max(adjustedStartHour.getHour() - HOUR_DISPLAY_RANGE, parsedWorkingHoursStart.getHour()) + ":" + adjustedStartHour.getHour())
                    .build());
        }

        // Show "Next" button only if there's a later time available
        if (parsedEndHour.isBefore(parsedWorkingHoursEnd)) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("Next ➡")
                    .callbackData("hour:" + selectedDate + ":" + parsedEndHour.getHour() + ":" + Math.min(parsedEndHour.getHour() + HOUR_DISPLAY_RANGE, parsedWorkingHoursEnd.getHour()))
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


    private static List<String> availableHours(List<WorkingHours> workingHours, String day) {
        for (WorkingHours workingHour : workingHours) {
            if (workingHour.getDay().equalsIgnoreCase(day)) {
                List<String> hours = new ArrayList<>();
                for (TimeRange timeRange : workingHour.getTimeRanges()) {
                    hours.add(timeRange.getStartTime() + "-" + timeRange.getEndTime());
                }
                return hours;
            }
        }
        return List.of();
    }

    private static boolean isBetween(List<String> timeRanges, String time) {
        LocalTime targetTime = LocalTime.parse(time);
        for (String range : timeRanges) {
            String[] parts = range.split("-");
            LocalTime startTime = LocalTime.parse(parts[0].trim());
            LocalTime endTime = LocalTime.parse(parts[1].trim());
            if (!targetTime.isBefore(startTime) && !targetTime.isAfter(endTime)) return true;
        }
        return false;
    }
}
