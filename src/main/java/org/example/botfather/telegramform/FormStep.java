package org.example.botfather.telegramform;
import java.util.function.Predicate;

public record FormStep<T>(String question, Predicate<T> validator, String errorMessage, String successMessage,
                          String fieldName) {
    public boolean validate(T input) {
        return validator.test(input);
    }
}
