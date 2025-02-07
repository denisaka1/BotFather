package org.example.botfather.telegramform.validators;

import java.util.regex.Pattern;

public class WorkingHoursValidator implements IValidator<String> {
    private static final Pattern WORKING_HOURS_PATTERN =
            Pattern.compile("^(monday|tuesday|wednesday|thursday|friday|saturday|sunday):\\s*((\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2})(,\\s*\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2})*|none)$");

    @Override
    public boolean validate(String input) {
        String[] lines = input.split("\n");

        // Ensure all days of the week are included
        if (lines.length != 7) {
            return false;
        }

        // Validate each line of the input (each day)
        for (String line : lines) {
            line = line.trim();
            if (!WORKING_HOURS_PATTERN.matcher(line).matches()) {
                return false;
            }
        }

        return true;
    }
}
