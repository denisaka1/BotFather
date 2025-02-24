package org.example.bots.manager.actions.helpers;

import lombok.RequiredArgsConstructor;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.BotCreationState;
import org.example.telegram.components.validators.BotMessageValidator;
import org.example.telegram.components.validators.StringValidator;
import org.example.telegram.components.validators.WorkingDurationsValidator;
import org.example.telegram.components.validators.WorkingHoursValidator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonCommandHelper {
    private final BusinessOwnerApi businessOwnerApi;

    public boolean isValidInput(BotCreationState state, String userMessage) {
        return switch (state) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> new BotMessageValidator().validate(userMessage);
            case ASK_BOT_NAME, ASK_WELCOME_MESSAGE -> new StringValidator().validate(userMessage);
            case ASK_WORKING_HOURS -> new WorkingHoursValidator().validate(userMessage);
            case ASK_JOBS -> new WorkingDurationsValidator().validate(userMessage);
            default -> true;
        };
    }

    public boolean botsExist(Long userId) {
        return businessOwnerApi.isRegistered(userId) && businessOwnerApi.getBots(userId).length > 0;
    }
}
