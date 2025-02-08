package org.example.telegram.components.validators;

import java.util.regex.Pattern;

public class WorkingDurationsValidator implements IValidator<String> {
    private static final Pattern WORKING_DURATIONS_PATTERN =
            Pattern.compile("^[\\w\\s'\\-]+:\\s*(\\d{2}:\\d{2})(,\\s*\\d{2}:\\d{2})*$");

    @Override
    public boolean validate(String input) {
        String[] lines = input.split("\n");

        // Validate each line of the input (each service and time)
        for (String line : lines) {
            line = line.trim();
            if (!WORKING_DURATIONS_PATTERN.matcher(line).matches()) {
                return false;
            }
        }

        return true;
    }
}
