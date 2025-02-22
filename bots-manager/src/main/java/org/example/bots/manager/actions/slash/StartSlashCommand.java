package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.BusinessOwner;
import org.example.data.layer.entities.OwnerRegistrationState;
import org.example.telegram.components.validators.EmailValidator;
import org.example.telegram.components.validators.PhoneNumberValidator;
import org.example.telegram.components.validators.StringValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Component
public class StartSlashCommand implements ISlashCommand {

    private final BusinessOwnerApi businessOwnerApi;
    private final MessageBatchProcessor messageBatchProcessor;

    private BusinessOwner businessOwner;

    @Override
    public void execute(Message message) {
        User telegramUser = message.getFrom();
        BusinessOwner incomingOwner = BusinessOwner.builder()
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .userTelegramId(telegramUser.getId())
                .build();
        businessOwner = businessOwnerApi.createIfNotPresent(incomingOwner);
        messageBatchProcessor.addMessage(
                SendMessage.builder()
                        .chatId(telegramUser.getId())
                        .text(SlashCommand.BACK_COMMAND_MESSAGE + businessOwner.getRegistrationState().getMessage())
                        .build()
        );
    }

    public boolean isCompleted() {
        return businessOwner.getRegistrationState().isCompleted();
    }

    public String processUserResponse(Message message) {
        String userInput = message.getText();
        OwnerRegistrationState currentState = businessOwner.getRegistrationState();

        if (Objects.equals(userInput, SlashCommand.BACK)) {
            OwnerRegistrationState previousState = currentState.getPreviousState().get();
            if (previousState == currentState) {
                return currentState.getMessage();
            }
            businessOwner.setRegistrationState(previousState);
            businessOwnerApi.update(businessOwner);
            return SlashCommand.RETURNING_TO_PREVIOUS_MESSAGE + previousState.getMessage();
        } else if (!isValidInput(currentState, userInput)) {
            return "❌ Invalid input!\n" + currentState.getMessage();
        }

        return processState(currentState, userInput);
    }

    private String processState(OwnerRegistrationState currentState, String userInput) {
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
