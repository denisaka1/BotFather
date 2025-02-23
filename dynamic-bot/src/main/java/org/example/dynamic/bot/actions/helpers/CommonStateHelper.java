package org.example.dynamic.bot.actions.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommonStateHelper {
    public SendMessage createSendMessage(Long chatId, String text) {
        return new SendMessage(chatId.toString(), text);
    }

    public InlineKeyboardMarkup createInlineKeyboard(String[][] buttonConfigs) {
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfigs);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }
}
