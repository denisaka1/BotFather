package org.example.bots.manager.actions.slash;

public final class SlashCommand {
    private SlashCommand() {
    }

    public static final String CANCEL = "/cancel";
    public static final String CREATE = "/create";
    public static final String START = "/start";
    public static final String BOTS = "/bots";
    public static final String SCHEDULE = "/schedule";
    public static final String BACK = "/back";

    public static final String BACK_COMMAND_MESSAGE = "You can return to previous by sending " + BACK + " command\n\n";
    public static final String RETURNING_TO_PREVIOUS_MESSAGE = "⤴️ Returning to previous message\n\n";
}
