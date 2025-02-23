package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.processor.MessageBatchProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduleSlashCommand implements ISlashCommand {
    private final MessageBatchProcessor messageBatchProcessor;

    @Override
    public void execute(Message message) {
        Long userId = message.getFrom().getId();
        messageBatchProcessor.addMessage(
                SendMessage.builder()
                        .chatId(userId)
                        .text("schedule")
                        .build()
        );
    }
}
