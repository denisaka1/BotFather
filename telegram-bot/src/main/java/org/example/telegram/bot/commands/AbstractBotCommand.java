package org.example.telegram.bot.commands;

import jakarta.persistence.MappedSuperclass;
import lombok.RequiredArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.components.forms.GenericForm;

@MappedSuperclass
@RequiredArgsConstructor
public abstract class AbstractBotCommand implements IBotCommand {

    protected final ApiRequestHelper apiRequestHelper;

    protected GenericForm userForm;
    protected boolean forceCompleted = false;

    protected boolean checkIfUserExists(Long userId) {
        return apiRequestHelper.get(
                "http://localhost:8080/api/business_owner/" + userId + "/exists",
                Boolean.class
        );
    }
}
