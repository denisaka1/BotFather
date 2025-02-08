package org.example.telegram.components.inline.keyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ButtonsGenerator {
    public static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static List<List<InlineKeyboardButton>> createKeyboard(String[][] buttonConfigs) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (String[] rowConfig : buttonConfigs) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (String config : rowConfig) {
                String[] parts = config.split(":", 2);
                row.add(createButton(parts[0], parts[1]));
            }
            keyboard.add(row);
        }
        return keyboard;
    }
}
