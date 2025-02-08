package org.example.botfather.telegrambot.dynamicbotstates;
import org.example.botfather.commands.AbstractBotCommand;
import org.example.botfather.commands.DynamicBotAuthCommand;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.telegrambot.DynamicBotsMessageHandler;
import org.example.botfather.utils.ApiRequestHelper;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public class AuthState implements DynamicBotState {
    private final AbstractBotCommand command;

    public AuthState(ApiRequestHelper apiRequestHelper, Bot bot) {
        this.command = new DynamicBotAuthCommand(apiRequestHelper, bot);
    }

    @Override
    public BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData) {
        String response = command.execute(message);
        if (command.isCompleted()) {
            context.setState(message.getFrom().getId(), new ScheduleOrCancelQuestionState());
        }
        return new SendMessage(message.getChatId().toString(), response);
    }
}
