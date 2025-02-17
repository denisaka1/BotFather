package org.example.telegram.components.inline.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ButtonsGenerator {
    private ButtonsGenerator() {
    }

    public static InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
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

    public static List<List<InlineKeyboardButton>> createKeyboard(Map<String, String> buttonConfigs) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Map.Entry<String, String> config : buttonConfigs.entrySet()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(config.getKey(), config.getValue()));
            keyboard.add(row);
        }

        return keyboard;
    }
}
