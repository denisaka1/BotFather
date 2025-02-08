package org.example.telegram.components.validators;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements IValidator<String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+972|972|0)([2-9]\\d{7}|5\\d{8})$");

    @Override
    public boolean validate(String input) {
        return input != null && PHONE_PATTERN.matcher(input).matches();
    }
}
