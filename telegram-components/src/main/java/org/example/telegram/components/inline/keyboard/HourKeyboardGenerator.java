package org.example.telegram.components.inline.keyboard;
import org.example.data.layer.entities.Appointment;
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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static InlineKeyboardMarkup generateHourKeyboard(String selectedDate, String startHour, List<WorkingHours> workingHours, String jobDetails) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().plusMinutes(15 - (LocalTime.now().getMinute() % 15)); // Round up to the next 15-minute slot
        LocalDate selected = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String day = selected.getDayOfWeek().name();
        List<String> availableHoursList = availableHours(workingHours, day);

        // Parse working hours start and end times from available working hours
        LocalTime parsedWorkingHoursStart = selected.equals(today) ? now : LocalTime.parse(availableHoursList.get(0).split("-")[0], TIME_FORMATTER);
        LocalTime parsedWorkingHoursEnd = LocalTime.parse(availableHoursList.get(availableHoursList.size() - 1).split("-")[1], TIME_FORMATTER);

        // Parse startHour and adjust it if necessary
        LocalTime parsedStartHour = (startHour != null) ? LocalTime.parse(startHour, TIME_FORMATTER) : parsedWorkingHoursStart;

        // Adjusted end hour based on range
        LocalTime parsedEndHour = parsedStartHour.plusHours(HOUR_DISPLAY_RANGE);

        // Adjust start hour if today to avoid showing past times
        LocalTime adjustedStartHour = selected.equals(today) ? LocalTime.of(Math.max(parsedStartHour.getHour(), now.getHour()), 0) : parsedStartHour;

        // Ensure the start and end times are within working hours
        adjustedStartHour = adjustedStartHour.isBefore(parsedWorkingHoursStart) ? parsedWorkingHoursStart : adjustedStartHour;
        parsedEndHour = parsedEndHour.isAfter(parsedWorkingHoursEnd) ? parsedWorkingHoursEnd : parsedEndHour;

        // Generate time buttons for available hours
        for (int hour = adjustedStartHour.getHour(); hour < parsedEndHour.getHour(); hour++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int minutes = 0; minutes < 60; minutes += 15) {
                if (selected.equals(today) && (hour < now.getHour() || (hour == now.getHour() && minutes < now.getMinute()))) {
                    continue; // Skip past times if today
                }
                String time = String.format("%02d:%02d", hour, minutes);

                if (!isBetween(availableHoursList, time)) continue;
                row.add(InlineKeyboardButton.builder()
                        .text(time)
                        .callbackData(Appointment.AppointmentCreationStep.HOUR_SELECTED.name() + "@" + selectedDate + "@" + jobDetails + "@" + time)
                        .build());
            }
            if (!row.isEmpty()) {
                rows.add(row);
            }
        }

        // Add navigation buttons if available
        addNavigationButtons(rows, selectedDate, adjustedStartHour, parsedEndHour, parsedWorkingHoursStart, parsedWorkingHoursEnd, jobDetails);

        // Add "Back" button
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("<< Back to Jobs")
                        .callbackData(Appointment.AppointmentCreationStep.BACK_TO_JOBS.name() + "@" + selectedDate)
                        .build()
        ));

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
        LocalTime targetTime = LocalTime.parse(time, TIME_FORMATTER);
        for (String range : timeRanges) {
            String[] parts = range.split("-");
            LocalTime startTime = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);
            if (!targetTime.isBefore(startTime) && !targetTime.isAfter(endTime)) {
                return true;
            }
        }
        return false;
    }

    private static void addNavigationButtons(List<List<InlineKeyboardButton>> rows, String selectedDate, LocalTime adjustedStartHour, LocalTime parsedEndHour, LocalTime parsedWorkingHoursStart, LocalTime parsedWorkingHoursEnd, String jobDetails) {
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        // Show "Previous" button if there's an earlier time available
        if (adjustedStartHour.isAfter(parsedWorkingHoursStart)) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("⬅ Previous")
                    .callbackData(Appointment.AppointmentCreationStep.UPDATE_HOURS.name() + "@" + selectedDate + "@" + jobDetails + "@" + Math.max(adjustedStartHour.getHour() - HOUR_DISPLAY_RANGE, parsedWorkingHoursStart.getHour()) + ":" + adjustedStartHour.getHour())
                    .build());
        }

        // Show "Next" button if there's a later time available
        if (parsedEndHour.isBefore(parsedWorkingHoursEnd)) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("Next ➡")
                    .callbackData(Appointment.AppointmentCreationStep.UPDATE_HOURS.name() + "@" + selectedDate+ "@" + jobDetails + "@" + parsedEndHour.getHour() + ":" + Math.min(parsedEndHour.getHour() + HOUR_DISPLAY_RANGE, parsedWorkingHoursEnd.getHour()))
                    .build());
        }

        // Add navigation row if it contains buttons
        if (!navigationRow.isEmpty()) {
            rows.add(navigationRow);
        }
    }
}
