package org.example.botfather.telegramform.validators;

public interface IValidator<T> {
    boolean validate(T input);
}
