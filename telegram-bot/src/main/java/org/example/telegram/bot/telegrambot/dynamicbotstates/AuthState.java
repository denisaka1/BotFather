package org.example.telegram.bot.telegrambot.dynamicbotstates;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.telegram.bot.commands.AbstractBotCommand;
import org.example.telegram.bot.commands.DynamicBotAuthCommand;
import org.example.telegram.bot.telegrambot.DynamicBotsMessageHandler;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public class AuthState implements IDynamicBotState {
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
