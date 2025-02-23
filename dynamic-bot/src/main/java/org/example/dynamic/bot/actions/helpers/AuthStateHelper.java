package org.example.dynamic.bot.actions.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.Client;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthStateHelper {
    private final ClientApi clientApi;

    public String getFullName(Message message) {
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        return lastName != null ? firstName + " " + lastName : firstName;
    }

    public void saveClient(String telegramId, String name, Map<String, String> userResponses) {
        if (userResponses.containsKey("phoneNumber") && userResponses.containsKey("email")) {
            Client client = Client.builder()
                    .name(name)
                    .phoneNumber(userResponses.get("phoneNumber"))
                    .email(userResponses.get("email"))
                    .build();
            Client savedClient = clientApi.updateClient(client, telegramId);
            log.info("Client saved: {}", savedClient);
        } else {
            log.warn("Missing user details, client not saved.");
        }
    }
}
