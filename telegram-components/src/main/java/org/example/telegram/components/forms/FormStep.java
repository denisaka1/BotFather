package org.example.telegram.components.forms;

import org.example.telegram.components.validators.IValidator;

public record FormStep<T>(String question, IValidator<T> validator, String errorMessage, String successMessage,
                          String fieldName) {
    public boolean validate(T input) {
        return validator.validate(input);
    }
}
