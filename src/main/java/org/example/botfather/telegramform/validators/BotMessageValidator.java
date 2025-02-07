package org.example.botfather.telegramform.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotMessageValidator implements IValidator<String> {
    private static final Pattern BOT_LINK_PATTERN = Pattern.compile("t\\.me/([a-zA-Z0-9_]+)");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\b(\\d+:[A-Za-z0-9_-]+)\\b");

    @Override
    public boolean validate(String input) {
        return extractBotLink(input) != null && extractToken(input) != null;
    }

    public static String extractBotLink(String message) {
        Matcher matcher = BOT_LINK_PATTERN.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static String extractToken(String message) {
        Matcher matcher = TOKEN_PATTERN.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }
}
