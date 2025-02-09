package org.example.botfather.commands;

import jakarta.persistence.MappedSuperclass;
import lombok.RequiredArgsConstructor;
import org.example.botfather.telegramcomponents.form.GenericForm;
import org.example.botfather.utils.ApiRequestHelper;

@MappedSuperclass
@RequiredArgsConstructor
public abstract class AbstractBotCommand implements IBotCommand {

    protected final ApiRequestHelper apiRequestHelper;

    protected GenericForm userForm;
    protected boolean forceCompleted = false;

//    TODO: finish implementing it

    protected boolean checkIfUserExists(Long userId) {
        return apiRequestHelper.get(
                "http://localhost:8080/api/business_owner/" + userId + "/exists",
                Boolean.class
        );
    }
}
