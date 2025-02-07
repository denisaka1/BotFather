package org.example.botfather.telegramform.validators;

import java.util.regex.Pattern;

public class EmailValidator implements IValidator<String> {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Override
    public boolean validate(String input) {
        return input != null && EMAIL_PATTERN.matcher(input).matches();
    }
}
