package org.example.telegram.components.validators;

import java.time.LocalTime;
import java.util.regex.Pattern;

public class WorkingHoursValidator implements IValidator<String> {
    private static final Pattern WORKING_HOURS_PATTERN =
            Pattern.compile("^(monday|tuesday|wednesday|thursday|friday|saturday|sunday):\\s*((\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2})(,\\s*\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2})*|None)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean validate(String input) {
        String[] lines = input.split("\n");

        // Ensure all days of the week are included (must be exactly 7)
        if (lines.length != 7) {
            return false;
        }

        // Validate each line
        for (String line : lines) {
            line = line.trim(); // Remove extra spaces

            if (!WORKING_HOURS_PATTERN.matcher(line).matches()) {
                return false; // Invalid format
            }

            // Validate time intervals
            if (!validateTimeIntervals(line)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateTimeIntervals(String line) {
        // Extract the time part after the colon
        String[] parts = line.split(":", 2); // Split only at the first colon
        if (parts.length < 2) {
            return false; // Invalid format
        }

        String timeRange = parts[1].trim();

        // If "None", return true (valid)
        if (timeRange.equalsIgnoreCase("None")) {
            return true;
        }

        // Process time ranges
        String[] timeIntervals = timeRange.split(",");
        LocalTime lastEndTime = null;

        for (String interval : timeIntervals) {
            interval = interval.trim();
            String[] times = interval.split("-");

            if (times.length != 2) {
                return false; // Invalid format
            }

            LocalTime startTime = parseTime(times[0].trim());
            LocalTime endTime = parseTime(times[1].trim());

            // Ensure the start and end times are on valid intervals
            if (isNotValidTimeInterval(startTime) || isNotValidTimeInterval(endTime)) {
                return false;
            }

            // Ensure the end time is strictly after the start time
            if (!endTime.isAfter(startTime)) {
                return false;
            }

            // Ensure that there is no overlap between different intervals
            if (lastEndTime != null && !startTime.isAfter(lastEndTime)) {
                return false;
            }

            lastEndTime = endTime;
        }

        return true;
    }

    private LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null; // Handle invalid time formats
        }
    }

    private boolean isNotValidTimeInterval(LocalTime time) {
        if (time == null) return true;
        int minute = time.getMinute();
        return minute != 0 && minute != 30; // Allow only full hours or half-hour intervals
    }
}
