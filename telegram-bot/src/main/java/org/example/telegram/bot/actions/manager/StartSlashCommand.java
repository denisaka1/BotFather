package org.example.telegram.bot.actions.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.BusinessOwner;
import org.example.telegram.components.forms.FormStep;
import org.example.telegram.components.forms.GenericForm;
import org.example.telegram.components.validators.EmailValidator;
import org.example.telegram.components.validators.PhoneNumberValidator;
import org.example.telegram.components.validators.StringValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class StartSlashCommand implements ISlashCommand {

    private GenericForm userForm;
    private final BusinessOwnerApi businessOwnerApi;

    @Override
    public String execute(Message message) {
        createForm();

        // check if the user is already registered
        User telegramUser = message.getFrom();
        if (businessOwnerApi.isPresent(telegramUser.getId())) {
            return "👋 Welcome back! You are already registered!\n Type any text to continue.";
        }

        String response = userForm.handleResponse(message.getText());
        Map<String, String> userInput = userForm.getUserResponses();
        if (userForm.isCompleted()) {
            BusinessOwner businessOwner = BusinessOwner.builder()
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .userTelegramId(telegramUser.getId())
                    .phoneNumber(userInput.get("phoneNumber"))
                    .email(userInput.get("email").toLowerCase())
                    .address(userInput.get("address"))
                    .build();
            BusinessOwner savedBusinessOwner = businessOwnerApi.create(businessOwner);
            log.info("Saved business owner: {}", savedBusinessOwner);
        }
        return response;
    }

    private void createForm() {
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email"),
                new FormStep<>("🏠 What is your address?", new StringValidator(), "❌ Invalid address!", "✅ Address is saved.", "address")
        ), "👋 Welcome to the Users Creator!\n\nPlease follow all the instructions. You can go back anytime by typing /back.", "🎉 Thank you for registering! Type any text to continue.");
    }
}
