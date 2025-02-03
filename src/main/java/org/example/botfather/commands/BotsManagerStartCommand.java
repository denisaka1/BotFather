package org.example.botfather.commands;
import org.example.botfather.telegramform.GenericForm;
import org.example.botfather.telegramform.FormStep;
import org.example.botfather.telegramform.Validators;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class BotsManagerStartCommand implements BotCommand {
    private final GenericForm userForm;

    public BotsManagerStartCommand() {
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new Validators.PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new Validators.EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email"),
                new FormStep<>("🏠 What is your address?", new Validators.StringValidator(), "❌ Invalid address!", "✅ Address is saved.", "address")
        ), "👋 Welcome to the Bots Manager!", "🎉 Thank you for registering!");
    }

    @Override
    public boolean isCompleted() {
        return userForm.isCompleted();
    }

    @Override
    public String execute(Message message) {
        // check if the user is already registered
        Long userId = message.getFrom().getId();
        String response = userForm.handleResponse(message.getText().toLowerCase());

        return response;
    }
}
