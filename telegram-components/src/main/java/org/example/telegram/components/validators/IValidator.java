package org.example.telegram.components.validators;

public interface IValidator<T> {
    boolean validate(T input);
}
