package org.example.botfather.telegramform;
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

