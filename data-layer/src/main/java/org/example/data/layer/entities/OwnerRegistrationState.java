package org.example.data.layer.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public enum OwnerRegistrationState {
    ASK_PHONE("📱 What is your phone number?"),
    ASK_EMAIL("📧 What is your email?"),
    ASK_ADDRESS("🏠 What is your address?"),
    COMPLETED("🎉 Thank you for registering! Type any text to continue.");

    @Getter
    private final String message;

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public Optional<OwnerRegistrationState> getNextState() {
        return switch (this) {
            case ASK_PHONE -> Optional.of(ASK_EMAIL);
            case ASK_EMAIL -> Optional.of(ASK_ADDRESS);
            case ASK_ADDRESS -> Optional.of(COMPLETED);
            case COMPLETED -> Optional.empty();
        };
    }

    public Optional<OwnerRegistrationState> getPreviousState() {
        return switch (this) {
            case ASK_PHONE, COMPLETED -> Optional.empty();
            case ASK_EMAIL -> Optional.of(ASK_PHONE);
            case ASK_ADDRESS -> Optional.of(ASK_EMAIL);
        };
    }

    public static Optional<OwnerRegistrationState> fromString(String stateName) {
        return Arrays.stream(OwnerRegistrationState.values())
                .filter(state -> state.name().equalsIgnoreCase(stateName))
                .findFirst();
    }
}
