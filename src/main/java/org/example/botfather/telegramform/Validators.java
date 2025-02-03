package org.example.botfather.telegramform;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validators {

    public interface Validator<T> {
        boolean validate(T input);
    }

    // ✅ String Validator (Non-empty check)
    public static class StringValidator implements Validator<String> {
        @Override
        public boolean validate(String input) {
            return input != null && !input.trim().isEmpty();
        }
    }

    public static class WorkingDurationsValidator implements Validator<String> {
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

    public static class WorkingHoursValidator implements Validator<String> {
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

    public static class BotMessageValidator implements Validator<String> {
        private static final Pattern BOT_LINK_PATTERN = Pattern.compile("t\\.me/([a-zA-Z0-9_]+)");
        private static final Pattern TOKEN_PATTERN = Pattern.compile("\\b(\\d+:[A-Za-z0-9_-]+)\\b");

        @Override
        public boolean validate(String input) {
            return extractBotLink(input) != null && extractToken(input) != null;
        }

        public static String extractBotLink(String message) {
            Matcher matcher = BOT_LINK_PATTERN.matcher(message);
            return matcher.find() ? matcher.group(1) : null;
        }

        public static String extractToken(String message) {
            Matcher matcher = TOKEN_PATTERN.matcher(message);
            return matcher.find() ? matcher.group(1) : null;
        }
    }

    // ✅ Email Validator (Regex-based)
    public static class EmailValidator implements Validator<String> {
        private static final Pattern EMAIL_PATTERN =
                Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

        @Override
        public boolean validate(String input) {
            return input != null && EMAIL_PATTERN.matcher(input).matches();
        }
    }

    // ✅ Phone Number Validator (Basic check for digits)
    public static class PhoneNumberValidator implements Validator<String> {
        private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+972|972|0)([2-9]\\d{7}|5\\d{8})$");

        @Override
        public boolean validate(String input) {
            return input != null && PHONE_PATTERN.matcher(input).matches();
        }
    }
}

