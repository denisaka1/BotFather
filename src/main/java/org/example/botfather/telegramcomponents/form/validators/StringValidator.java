package org.example.botfather.telegramcomponents.form.validators;

// ✅ String Validator (Non-empty check)
public class StringValidator implements IValidator<String> {
    @Override
    public boolean validate(String input) {
        return input != null && !input.trim().isEmpty();
    }
}
