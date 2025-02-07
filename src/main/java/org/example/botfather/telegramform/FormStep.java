package org.example.botfather.telegramform;
import org.example.botfather.telegramform.validators.IValidator;

public record FormStep<T>(String question, IValidator<T> validator, String errorMessage, String successMessage,
                          String fieldName) {
    public boolean validate(T input) {
        return validator.validate(input);
    }
}
