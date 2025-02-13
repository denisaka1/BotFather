package org.example.telegram.bot.actions.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.BusinessOwner;
import org.example.data.layer.entities.OwnerRegistrationState;
import org.example.telegram.components.validators.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Component
public class StartSlashCommand implements ISlashCommand {

    private final BusinessOwnerApi businessOwnerApi;

    private BusinessOwner businessOwner;

    @Override
    public String execute(Message message) {
        User telegramUser = message.getFrom();
        BusinessOwner incomingOwner = BusinessOwner.builder()
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .userTelegramId(telegramUser.getId())
                .build();
        businessOwner = businessOwnerApi.createIfNotPresent(incomingOwner);
        return businessOwner.getRegistrationState().getMessage();
    }

    public boolean isCompleted() {
        return businessOwner.getRegistrationState().isCompleted();
    }

    public String processUserResponse(Message message) {
        String userInput = message.getText();
        OwnerRegistrationState currentState = businessOwner.getRegistrationState();

        if (!isValidInput(currentState, userInput)) {
            return "❌ Invalid input!\n" + currentState.getMessage();
        }

        AtomicReference<String> response = new AtomicReference<>("");
        currentState.getNextState().ifPresentOrElse(nextState -> {
            switch (currentState) {
                case ASK_PHONE -> businessOwner.setPhoneNumber(userInput);
                case ASK_EMAIL -> businessOwner.setEmail(userInput);
                case ASK_ADDRESS -> businessOwner.setAddress(userInput);
                default -> response.set(nextState.getMessage());
            }
            businessOwner.setRegistrationState(nextState);
            businessOwner = businessOwnerApi.update(businessOwner);
            response.set(nextState.getMessage());
        }, () -> {
            // If no next state, user has completed registration
        });
        return response.get();
    }

    private boolean isValidInput(OwnerRegistrationState state, String userMessage) {
        return switch (state) {
            case ASK_PHONE -> new PhoneNumberValidator().validate(userMessage);
            case ASK_EMAIL -> new EmailValidator().validate(userMessage);
            case ASK_ADDRESS -> new StringValidator().validate(userMessage);
            default -> true;
        };
    }
}
