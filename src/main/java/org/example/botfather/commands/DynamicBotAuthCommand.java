package org.example.botfather.commands;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.entities.Client;
import org.example.botfather.telegramform.FormStep;
import org.example.botfather.telegramform.GenericForm;
import org.example.botfather.telegramform.Validators;
import org.example.botfather.utils.ApiRequestHelper;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Arrays;

public class DynamicBotAuthCommand implements BotCommand {
    private final GenericForm userForm;
    private final ApiRequestHelper apiRequestHelper;

    public DynamicBotAuthCommand(ApiRequestHelper apiRequestHelper, Bot bot) {
        this.apiRequestHelper = apiRequestHelper;
        String firstMessage = bot.getWelcomeMessage() +
                "\n\nIt looks like you're not registered yet." +
                "\nPlease follow the instructions to complete your registration." +
                "\nYou can go back at any time by typing /back.";
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new Validators.PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new Validators.EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email")),
                firstMessage, "🎉 Thank you for registering! Type any text to continue.");
    }

    @Override
    public String execute(Message message) {
        String response = userForm.handleResponse(message.getText().toLowerCase());
        if (userForm.isCompleted()) {
            Client client = Client.builder()
                    .name(message.getFrom().getFirstName() + " " + message.getFrom().getLastName())
                    .telegramId(message.getFrom().getId().toString())
                    .phoneNumber(userForm.getUserResponses().get("phoneNumber"))
                    .email(userForm.getUserResponses().get("email"))
                    .build();
            Client savedClient = this.apiRequestHelper.post(
                    "http://localhost:8080/api/client",
                    client,
                    Client.class
            );
            System.out.println("Client saved: " + savedClient);
        }
        return response;
    }

    @Override
    public boolean isCompleted() {
        return userForm.isCompleted();
    }
}
