package org.example.telegram.bot.actions.manager;

import jakarta.persistence.MappedSuperclass;
import lombok.RequiredArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.telegram.components.forms.GenericForm;

@MappedSuperclass
@RequiredArgsConstructor
public abstract class AbstractSlashCommand implements ISlashCommand {

    protected GenericForm userForm;
}
