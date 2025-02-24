package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.actions.helpers.BotsCommandHelper;
import org.example.bots.manager.actions.helpers.CommonCommandHelper;
import org.example.client.api.processor.MessageBatchProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduleSlashCommand implements ISlashCommand {
    private final MessageBatchProcessor messageBatchProcessor;
    private final CommonCommandHelper commonCommandHelper;
    private final BotsCommandHelper botsCommandHelper;

    @Override
    public void execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!commonCommandHelper.botsExist(userId)) {
            String text = """
                    👋 Welcome to the Appointment Manager!
                    You don't have any bots created.
                    You need to create a new bot using the /create command.
                    Type any text to return to the menu.""";
            messageBatchProcessor.addMessage(
                    SendMessage.builder()
                            .chatId(userId)
                            .text(text)
                            .build()
            );
            return;
        }
        botsCommandHelper.showBotsList(message, true);
    }

    public void processCallbackResponse(Update update) {
        String callbackData = update.getCallbackQuery().getData();
    }
}
