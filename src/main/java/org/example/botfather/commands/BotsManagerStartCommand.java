package org.example.botfather.commands;
import org.example.botfather.data.entities.BusinessOwner;
import org.example.botfather.telegramform.GenericForm;
import org.example.botfather.telegramform.FormStep;
import org.example.botfather.telegramform.Validators;
import org.example.botfather.utils.ApiRequestHelper;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Map;

@Component
public class BotsManagerStartCommand implements BotCommand {
    private final GenericForm userForm;
    private final ApiRequestHelper apiRequestHelper;
    private boolean formCompleted = false;

    public BotsManagerStartCommand(ApiRequestHelper apiRequestHelper) {
        this.apiRequestHelper = apiRequestHelper;
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new Validators.PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new Validators.EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email"),
                new FormStep<>("🏠 What is your address?", new Validators.StringValidator(), "❌ Invalid address!", "✅ Address is saved.", "address")
        ), "👋 Welcome to the Users Creator!", "🎉 Thank you for registering! Type any text to continue.");
    }

    @Override
    public boolean isCompleted() {
        return userForm.isCompleted() || this.formCompleted;
    }

    public boolean checkIfUserExists(Long userId) {
        return this.apiRequestHelper.get(
                "http://localhost:8080/api/business_owner/exists",
                    Boolean.class,
                    Map.of("userTelegramId", userId.toString())
        );
    }

    @Override
    public String execute(Message message) {
        // check if the user is already registered
        Long userId = message.getFrom().getId();
        if (checkIfUserExists(userId)) {
            this.formCompleted = true;
            return "👋 Welcome back! You are already registered!\n Type any text to continue.";
        }
        String response = userForm.handleResponse(message.getText().toLowerCase());
        if (userForm.isCompleted()) {
            BusinessOwner businessOwner = BusinessOwner.builder()
                    .firstName(message.getFrom().getFirstName())
                    .lastName(message.getFrom().getLastName())
                    .userTelegramId(message.getFrom().getId().toString())
                    .phoneNumber(userForm.getUserResponses().get("phoneNumber"))
                    .email(userForm.getUserResponses().get("email"))
                    .address(userForm.getUserResponses().get("address"))
                    .build();
//            this.apiRequestHelper.post(
//                    "http://localhost:8080/api/business_owner",
//                    userForm.getUserResponses(),
//                    Map.class
//            );
            System.out.println(businessOwner.toString());
        }
        // check if the form is completed and create new user with userForm.userResponses()
        return response;
    }
}
