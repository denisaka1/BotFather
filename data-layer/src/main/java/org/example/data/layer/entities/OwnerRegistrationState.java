package org.example.data.layer.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum OwnerRegistrationState {
    ASK_PHONE("📱 What is your phone number?"),
    ASK_EMAIL("📧 What is your email?"),
    ASK_ADDRESS("🏠 What is your address?"),
    COMPLETED("🎉 Thank you for registering! Type any text to continue.");

    private final String message;
    private OwnerRegistrationState previousState;
    private OwnerRegistrationState nextState;

    static {
        ASK_PHONE.previousState = ASK_PHONE;
        ASK_PHONE.nextState = ASK_EMAIL;

        ASK_EMAIL.previousState = ASK_PHONE;
        ASK_EMAIL.nextState = ASK_ADDRESS;

        ASK_ADDRESS.previousState = ASK_EMAIL;
        ASK_ADDRESS.nextState = COMPLETED;

        COMPLETED.previousState = ASK_ADDRESS;
        COMPLETED.nextState = COMPLETED;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public static Optional<OwnerRegistrationState> fromString(String stateName) {
        return Arrays.stream(OwnerRegistrationState.values())
                .filter(state -> state.name().equalsIgnoreCase(stateName))
                .findFirst();
    }
}
