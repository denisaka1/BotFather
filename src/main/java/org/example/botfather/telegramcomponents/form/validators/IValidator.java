package org.example.botfather.telegramcomponents.form.validators;

public interface IValidator<T> {
    boolean validate(T input);
}
