package org.example.telegram.bot.telegrambot.dynamicbotstates;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.telegram.bot.telegrambot.DynamicBotsMessageHandler;
import org.example.telegram.components.forms.FormStep;
import org.example.telegram.components.forms.GenericForm;
import org.example.telegram.components.validators.EmailValidator;
import org.example.telegram.components.validators.PhoneNumberValidator;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

@Slf4j
public class AuthState implements IDynamicBotState {
    private final GenericForm userForm;
    private final ApiRequestHelper apiRequestHelper;

    public AuthState(ApiRequestHelper apiRequestHelper, Bot bot) {
        this.apiRequestHelper = apiRequestHelper;
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
    public BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData) {
        String response = execute(message);
        if (userForm.isCompleted()) {
            context.setState(message.getFrom().getId(), new ScheduleOrCancelQuestionState());
        }
        return new SendMessage(message.getChatId().toString(), response);
    }

    public String execute(Message message) {
        String response = userForm.handleResponse(message.getText().toLowerCase());
        if (userForm.isCompleted()) {
            Client client = Client.builder()
                    .name(message.getFrom().getFirstName() + " " + message.getFrom().getLastName())
                    .telegramId(message.getFrom().getId().toString())
                    .phoneNumber(userForm.getUserResponses().get("phoneNumber"))
                    .email(userForm.getUserResponses().get("email"))
                    .build();
            Client savedClient = apiRequestHelper.post(
                    "http://localhost:8080/api/client",
                    client,
                    Client.class
            );
            log.info("Client saved: {}", savedClient);
        }
        return response;
    }
}
