package org.example.botfather.telegramform;
import java.util.regex.Pattern;

public class Validators {

    public interface Validator<T> {
        boolean validate(T input);
        String getErrorMessage();
    }

    // ✅ String Validator (Non-empty check)
    public static class StringValidator implements Validator<String> {
        @Override
        public boolean validate(String input) {
            return input != null && !input.trim().isEmpty();
        }

        @Override
        public String getErrorMessage() {
            return "Input cannot be empty.";
        }
    }

    // ✅ Email Validator (Regex-based)
    public static class EmailValidator implements Validator<String> {
        private static final Pattern EMAIL_PATTERN =
                Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

        @Override
        public boolean validate(String input) {
            return input != null && EMAIL_PATTERN.matcher(input).matches();
        }

        @Override
        public String getErrorMessage() {
            return "Invalid email.";
        }
    }

    // ✅ Phone Number Validator (Basic check for digits)
    public static class PhoneNumberValidator implements Validator<String> {
        private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+972|972|0)[2-9][0-9]{6,7}$\n");

        @Override
        public boolean validate(String input) {
            return input != null && PHONE_PATTERN.matcher(input).matches();
        }

        @Override
        public String getErrorMessage() {
            return "Invalid phone number.";
        }
    }
}

