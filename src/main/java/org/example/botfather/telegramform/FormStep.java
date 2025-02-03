package org.example.botfather.telegramform;
import org.example.botfather.telegramform.Validators.Validator;

public record FormStep<T>(String question, Validator<T> validator, String errorMessage, String successMessage,
                          String fieldName) {
    public boolean validate(T input) {
        return validator.validate(input);
    }
}
