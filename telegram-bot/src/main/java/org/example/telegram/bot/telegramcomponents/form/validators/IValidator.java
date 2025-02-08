package org.example.telegram.bot.telegramcomponents.form.validators;

public interface IValidator<T> {
    boolean validate(T input);
}
