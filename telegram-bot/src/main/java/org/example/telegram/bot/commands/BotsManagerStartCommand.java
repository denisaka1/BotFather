package org.example.telegram.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.bot.data.entities.BusinessOwner;
import org.example.telegram.bot.telegramcomponents.form.FormStep;
import org.example.telegram.bot.telegramcomponents.form.GenericForm;
import org.example.telegram.bot.telegramcomponents.form.validators.EmailValidator;
import org.example.telegram.bot.telegramcomponents.form.validators.PhoneNumberValidator;
import org.example.telegram.bot.telegramcomponents.form.validators.StringValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class BotsManagerStartCommand extends AbstractBotCommand {

    public BotsManagerStartCommand(ApiRequestHelper apiRequestHelper) {
        super(apiRequestHelper);
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email"),
                new FormStep<>("🏠 What is your address?", new StringValidator(), "❌ Invalid address!", "✅ Address is saved.", "address")
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
            log.info("Saved business owner: {}", savedBusinessOwner);
        }
        return response;
    }
}
