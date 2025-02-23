package org.example.dynamic.bot.actions.states;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Bot;
import org.example.dynamic.bot.actions.helpers.AuthStateHelper;
import org.example.dynamic.bot.actions.helpers.CommonStateHelper;
import org.example.dynamic.bot.services.DynamicMessageService;
import org.example.telegram.components.forms.FormStep;
import org.example.telegram.components.forms.GenericForm;
import org.example.telegram.components.validators.EmailValidator;
import org.example.telegram.components.validators.PhoneNumberValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.HashMap;

@Component
@Slf4j
@AllArgsConstructor
public class AuthState implements IDynamicBotState {
    private final HashMap<Long, GenericForm> userForms = new HashMap<>();
    private final AuthStateHelper authStateHelper;
    protected final CommonStateHelper commonStateHelper;
    private final MessageBatchProcessor messageBatchProcessor;

    @Override
    public void handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        Long userId = message.getFrom().getId();
        GenericForm form = userForms.computeIfAbsent(userId, k -> createForm(bot));
        String response = execute(form, message);
        if (form.isCompleted()) {
            context.setState(message.getFrom().getId().toString(), bot.getId(), context.getScheduleOrCancelQuestionState());
        }
        messageBatchProcessor.addMessage(commonStateHelper.createSendMessage(message.getChatId(), response));
    }

    private GenericForm createForm(Bot bot) {
        String firstMessage = bot.getWelcomeMessage() +
                "\n\nIt looks like you're not registered yet." +
                "\nPlease follow the instructions to complete your registration." +
                "\nYou can go back at any time by typing /back.";
        return new GenericForm(Arrays.asList(
                new FormStep<>("📱 What is your phone number?", new PhoneNumberValidator(), "❌ Invalid phone number!", "✅ Phone number is saved.", "phoneNumber"),
                new FormStep<>("📧 What is your email?", new EmailValidator(), "❌ Invalid email!", "✅ Email is saved.", "email")),
                firstMessage, "🎉 Thank you for registering! Type any text to continue.");
    }

    private String execute(GenericForm userForm, Message message) {
        String response = userForm.handleResponse(message.getText().toLowerCase());
        if (userForm.isCompleted()) {
            String clientName = authStateHelper.getFullName(message);
            String telegramId = message.getFrom().getId().toString();
            authStateHelper.saveClient(telegramId, clientName, userForm.getUserResponses());
        }
        return response;
    }
}