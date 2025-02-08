package org.example.telegram.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.bot.data.entities.Bot;
import org.example.telegram.bot.data.entities.Client;
import org.example.telegram.bot.telegramcomponents.form.FormStep;
import org.example.telegram.bot.telegramcomponents.form.GenericForm;
import org.example.telegram.bot.telegramcomponents.form.validators.EmailValidator;
import org.example.telegram.bot.telegramcomponents.form.validators.PhoneNumberValidator;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

@Slf4j
public class DynamicBotAuthCommand extends AbstractBotCommand {
    private final GenericForm userForm;

    public DynamicBotAuthCommand(ApiRequestHelper apiRequestHelper, Bot bot) {
        super(apiRequestHelper);
        String firstMessage = bot.getWelcomeMessage() +
                "\n\nIt looks like you're not registered yet." +
                "\nPlease follow the instructions to complete your registration." +
                "\nYou can go back at any time by typing /back.";
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email")),
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
            log.info("Client saved: {}", savedClient);
        }
        return response;
    }

    @Override
    public boolean isCompleted() {
        return userForm.isCompleted();
    }
}
