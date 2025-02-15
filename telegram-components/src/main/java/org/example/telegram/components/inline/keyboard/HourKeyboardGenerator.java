package org.example.telegram.components.inline.keyboard;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.TimeRange;
import org.example.data.layer.entities.WorkingHours;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class HourKeyboardGenerator {
    private static final int HOUR_DISPLAY_RANGE = 6;
    private static final int MAX_TIMES_IN_ROW = 4;
    private static final String DELIMITER = "@";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static InlineKeyboardMarkup generateHoursKeyboard(String selectedDate, String startTime, List<WorkingHours> workingHours, String jobDetails, List<Appointment> appointments) {
        float interval = Float.parseFloat(jobDetails.split(":")[2]); // 0.5
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1); // Round up
        LocalDate selected = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String day = selected.getDayOfWeek().name();
        Optional<WorkingHours> matchingWorkingHours = workingHours.stream()
                .filter(wh -> wh.getDay().equalsIgnoreCase(day))
                .findFirst();
        WorkingHours currentWorkingHours = matchingWorkingHours.orElseGet(() -> workingHours.get(0));

        float workingHoursStart = selected.equals(today) ? convertTimeToFloat(now) : findExtremeStartTime(currentWorkingHours.getTimeRanges(), false);
        float workingHoursEnd = findExtremeStartTime(currentWorkingHours.getTimeRanges(), true);

        float displayStartTime = (startTime != null) ? convertTimeToFloat(LocalTime.parse(startTime, TIME_FORMATTER)) : workingHoursStart;
        float displayEndTime = Math.min((displayStartTime + HOUR_DISPLAY_RANGE), workingHoursEnd);

        Map<Float, Boolean> availableTimesMap = buildAvailableHoursMap(workingHoursEnd, interval, currentWorkingHours.getTimeRanges(), appointments);
        List<List<InlineKeyboardButton>> rows = organizeAvailableTimes(availableTimesMap, displayStartTime, displayEndTime, selectedDate, jobDetails);
        addNavigationButtons(rows, selectedDate, displayStartTime, displayEndTime, workingHoursStart, workingHoursEnd, jobDetails);
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private static List<List<InlineKeyboardButton>> organizeAvailableTimes(Map<Float, Boolean> availableTimesMap, float displayStartTime, float displayEndTime, String selectedDate, String jobDetails) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (Map.Entry<Float, Boolean> entry : availableTimesMap.entrySet()) {
            Float time = entry.getKey();
            Boolean isAvailable = entry.getValue();

            if (isAvailable && time >= displayStartTime && time < displayEndTime) {
                String displayTime = convertFloatToTimeString(time);
                InlineKeyboardButton timeButton = InlineKeyboardButton.builder()
                        .text(displayTime)
                        .callbackData(Appointment.AppointmentCreationStep.HOUR_SELECTED.name() + DELIMITER + selectedDate + DELIMITER + jobDetails + DELIMITER + displayTime)
                        .build();
                currentRow.add(timeButton);
                if (currentRow.size() == MAX_TIMES_IN_ROW) {
                    rows.add(new ArrayList<>(currentRow));
                    currentRow.clear();
                }
            }
        }
        if (!currentRow.isEmpty()) rows.add(currentRow);
        return rows;
    }

    private static void addNavigationButtons(List<List<InlineKeyboardButton>> rows, String selectedDate, float startTime, float endTime, float workingHoursStart, float workingHoursEnd, String jobDetails) {
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        if (startTime > workingHoursStart) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("⬅ Previous")
                    .callbackData(Appointment.AppointmentCreationStep.UPDATE_HOURS.name() + DELIMITER + selectedDate
                            + DELIMITER + jobDetails + DELIMITER + convertFloatToTimeString(Math.max(startTime - HOUR_DISPLAY_RANGE, workingHoursStart)))
                    .build());
        }
        if (endTime < workingHoursEnd) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("Next ➡")
                    .callbackData(Appointment.AppointmentCreationStep.UPDATE_HOURS.name() + DELIMITER + selectedDate
                            + DELIMITER + jobDetails + DELIMITER + convertFloatToTimeString(endTime))
                    .build());
        }
        if (!navigationRow.isEmpty()) {
            rows.add(navigationRow);
        }
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("<< Back to Jobs")
                        .callbackData(Appointment.AppointmentCreationStep.BACK_TO_JOBS.name() + DELIMITER + selectedDate)
                        .build()
        ));
    }

    private static Map<Float, Boolean> buildAvailableHoursMap(float workingHoursEnd, float interval, List<TimeRange> timeRanges, List<Appointment> appointments) {
        Map<Float, Boolean> availableHours = new TreeMap<>();
        for (TimeRange timeRange : timeRanges) {
            float startTimeRange = convertTimeToFloat(LocalTime.parse(timeRange.getStartTime()));
            float endTimeRange = convertTimeToFloat(LocalTime.parse(timeRange.getEndTime()));
            for (float i = startTimeRange; i <= endTimeRange; i += interval) {
                availableHours.put(
                        i,
                        (i + interval >= workingHoursEnd) || areTimesWithinRange(startTimeRange, endTimeRange, i, i + interval)
                );
            }
        }
        for (Appointment a : appointments) {
            float appointmentStartTime = convertTimeToFloat(a.getAppointmentDate().toLocalTime());
            float appointmentEndTime = (float) (appointmentStartTime + a.getJob().getDuration());

            for (float time = appointmentStartTime; time < appointmentEndTime; time += interval) {
                if (availableHours.containsKey(time)) {
                    availableHours.put(time, false);
                }
            }
        }
        return availableHours;
    }

    public static boolean areTimesWithinRange(float startTimeRange, float endTimeRange, float startTimeSlot, float endTimeSlot) {
        return startTimeSlot >= startTimeRange && endTimeRange >= endTimeSlot;
    }

    public static Float findExtremeStartTime(List<TimeRange> timeRanges, boolean findMax) {
        return timeRanges.stream()
                .map(findMax ? TimeRange::getEndTime : TimeRange::getStartTime)
                .map(LocalTime::parse)
                .min(findMax ? Comparator.reverseOrder() : Comparator.naturalOrder())
                .map(HourKeyboardGenerator::convertTimeToFloat)
                .orElse(8f);
    }

    private static String convertFloatToTimeString(float time) {
            int hours = (int) time;
            int minutes = Math.round((time - hours) * 60);
            return String.format("%02d:%02d", hours, minutes);
    }

    private static Float convertTimeToFloat(LocalTime time) { return time.getHour() + (time.getMinute() / 60.0f); }
}
