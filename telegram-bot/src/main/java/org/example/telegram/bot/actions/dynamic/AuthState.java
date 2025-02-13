package org.example.telegram.bot.actions.dynamic;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.telegram.bot.services.dynamic.DynamicMessageService;
import org.example.telegram.components.forms.FormStep;
import org.example.telegram.components.forms.GenericForm;
import org.example.telegram.components.validators.EmailValidator;
import org.example.telegram.components.validators.PhoneNumberValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Arrays;
import java.util.HashMap;

@Component
@Slf4j
@AllArgsConstructor
public class AuthState implements IDynamicBotState {
    private final HashMap<Long, GenericForm> userForms = new HashMap<>();
    private final ClientApi clientApi;

    @Override
    public BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        Long userId = message.getFrom().getId();
        GenericForm form;
        if (userForms.containsKey(userId)) {
            form = userForms.get(userId);
        } else {
            form = createForm(bot);
            userForms.put(userId, form);
        }
        String response = execute(form, message);
        if (form.isCompleted()) {
            context.setState(message.getFrom().getId(), context.getScheduleOrCancelQuestionState());
        }
        return new SendMessage(message.getChatId().toString(), response);
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
        String clientName = message.getFrom().getFirstName();
        if (message.getFrom().getLastName() != null) clientName +=  " " + message.getFrom().getLastName();
        if (userForm.isCompleted()) {
            Client client = Client.builder()
                    .name(clientName)
                    .telegramId(message.getFrom().getId().toString())
                    .phoneNumber(userForm.getUserResponses().get("phoneNumber"))
                    .email(userForm.getUserResponses().get("email"))
                    .build();
            Client savedClient = clientApi.createClient(client);
            log.info("Client saved: {}", savedClient);
        }
        return response;
    }
}
