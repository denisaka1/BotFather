package org.example.botfather.commands;
import org.example.botfather.data.entities.BusinessOwner;
import org.example.botfather.telegramform.GenericForm;
import org.example.botfather.telegramform.FormStep;
import org.example.botfather.telegramform.Validators;
import org.example.botfather.utils.ApiRequestHelper;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.Map;

@Component
public class BotsManagerStartCommand extends AbstractBotCommand {
    private final GenericForm userForm;

    public BotsManagerStartCommand(ApiRequestHelper apiRequestHelper) {
        super(apiRequestHelper);
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new Validators.PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new Validators.EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email"),
                new FormStep<>("🏠 What is your address?", new Validators.StringValidator(), "❌ Invalid address!", "✅ Address is saved.", "address")
        ), "👋 Welcome to the Users Creator!\n\nPlease follow all the instructions. You can go back anytime by typing /back.", "🎉 Thank you for registering! Type any text to continue.");
    }

    @Override
    public boolean isCompleted() {
        return userForm.isCompleted() || forceCompleted;
    }

    @Override
    public String execute(Message message) {
        // check if the user is already registered
        User telegramUser = message.getFrom();
        if (checkIfUserExists(telegramUser.getId())) {
            forceCompleted = true;
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
            BusinessOwner savedBusinessOwner = apiRequestHelper.post(
                    "http://localhost:8080/api/business_owner",
                    businessOwner,
                    BusinessOwner.class
            );
            System.out.println(savedBusinessOwner.toString());
        }
        return response;
    }
}
