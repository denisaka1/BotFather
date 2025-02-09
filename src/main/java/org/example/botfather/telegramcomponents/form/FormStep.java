package org.example.botfather.telegramcomponents.form;
import org.example.botfather.telegramcomponents.form.validators.IValidator;

public record FormStep<T>(String question, IValidator<T> validator, String errorMessage, String successMessage,
                          String fieldName) {
    public boolean validate(T input) {
        return validator.validate(input);
    }
}
