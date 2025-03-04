package org.example.bots.manager.actions.helpers;

import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.BotCreationState;
import org.example.telegram.components.validators.*;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

import static org.example.data.layer.entities.BotCreationState.*;

@Component
public class CommonCommandHelper {
    private final BusinessOwnerApi businessOwnerApi;
    private final Map<BotCreationState, IValidator> validators = new EnumMap<>(BotCreationState.class);

    public CommonCommandHelper(BusinessOwnerApi businessOwnerApi) {
        this.businessOwnerApi = businessOwnerApi;

        validators.put(ASK_BOT_FATHER_BOT_CREATION_MESSAGE, new BotMessageValidator());
        validators.put(ASK_BOT_NAME, new StringValidator());
        validators.put(ASK_WELCOME_MESSAGE, new StringValidator());
        validators.put(ASK_WORKING_HOURS, new WorkingHoursValidator());
        validators.put(ASK_JOBS, new WorkingDurationsValidator());
    }

    public boolean isValidInput(BotCreationState state, String userMessage) {
        if (!validators.containsKey(state)) {
            return false;
        }

        return validators.get(state).validate(userMessage);
    }

    public boolean botsExist(Long userId) {
        return businessOwnerApi.isRegistered(userId) && businessOwnerApi.getBots(userId).length > 0;
    }
}
