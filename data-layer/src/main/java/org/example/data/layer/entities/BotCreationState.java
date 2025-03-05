package org.example.data.layer.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum BotCreationState {
    ASK_BOT_FATHER_BOT_CREATION_MESSAGE("""
            👋 Welcome to the Bots Creator!
            
            Please follow the steps to create a new bot (Better to do it on a PC):
            
            1️⃣ Search for BotFather in Telegram and open it.
            2️⃣ Send the command /newbot.
            3️⃣ Follow the instructions to choose a name and username for your bot.
            4️⃣ Copy the final message containing your bot token.
            """,
            "✅ Bot creation message is verified!",
            "What is the new token for the bot?",
            "✅ Bot token successfully changed!"
    ),
    ASK_BOT_NAME(
            "📝 What is your bot name?",
            "✅ Bot name saved successfully!",
            "What is the new name for the bot?",
            "✅ Bot name changed successfully!"
    ),
    ASK_WELCOME_MESSAGE(
            "💬 What should be your bot's welcome message?",
            "✅ Welcome message saved successfully!",
            "What is the new welcome message for the bot?",
            "✅ Welcome message changed successfully!"
    ),
    ASK_WORKING_HOURS(
            """
                    ⏳ What are your working hours?
                    
                    ℹ️️ Please provide a list of days in the following format:
                    { Day: HH:MM - HH:MM } (24-hour format) or "None" if you don't work on that day.
                    ℹ️️ The last hour represents the latest time you are available to provide services.
                    ℹ️️ Please ensure you use full hours or half-hour intervals only.
                    
                    Example:
                    Monday: 09:30 - 17:00
                    Tuesday: 09:00 - 17:00
                    Wednesday: 09:00 - 16:00, 17:00 - 20:00
                    Thursday: 09:00 - 17:00
                    Friday: 10:00 - 14:00
                    Saturday: None
                    Sunday: None
                    """,
            "✅ Working hours are saved.",
            "What are the new working hours for the bot?",
            "✅ Working hours are changed."
    ),
    ASK_JOBS(
            """
                    📋 What are your working durations?
                    Please provide a list of services with their respective durations in the following format:
                    Service Name: HH:MM (or multiple time slots separated by commas)
                    
                    Example:
                    Men's haircut: 00:30
                    Women's haircut: 01:30
                    Lesson: 01:00, 02:00
                    Yoga class: 01:00
                    """,
            "✅ Working durations are saved.",
            "What are the new jobs for the bot?",
            "✅ Working durations are changed."
    ),
    COMPLETED(
            "🎉 Your new bot has been created successfully!\nYou can now access it using the link from the first message.\n\n🙏 Thank you for creating new bot with us! Type any text to continue.",
            "",
            "",
            ""
    );

    private final String message;
    private final String successSaveMessage;
    private final String editMessage;
    private final String successChangeMessage;
    private BotCreationState prevState;
    private BotCreationState nextState;

    static {
        ASK_BOT_FATHER_BOT_CREATION_MESSAGE.prevState = ASK_BOT_FATHER_BOT_CREATION_MESSAGE;
        ASK_BOT_FATHER_BOT_CREATION_MESSAGE.nextState = ASK_BOT_NAME;

        ASK_BOT_NAME.prevState = ASK_BOT_FATHER_BOT_CREATION_MESSAGE;
        ASK_BOT_NAME.nextState = ASK_WELCOME_MESSAGE;

        ASK_WELCOME_MESSAGE.prevState = ASK_BOT_NAME;
        ASK_WELCOME_MESSAGE.nextState = ASK_WORKING_HOURS;

        ASK_WORKING_HOURS.prevState = ASK_WELCOME_MESSAGE;
        ASK_WORKING_HOURS.nextState = ASK_JOBS;

        ASK_JOBS.prevState = ASK_WORKING_HOURS;
        ASK_JOBS.nextState = COMPLETED;

        COMPLETED.prevState = ASK_JOBS;
        COMPLETED.nextState = COMPLETED;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public String getExampleMessage() {
        if (this == ASK_BOT_FATHER_BOT_CREATION_MESSAGE) {
            return """
                    Please paste the message from BotFather that looks like this:
                    
                    Done! Congratulations on your new bot.
                    You will find it at t.me/<bot_username>.
                    You can now add a description, about section and profile picture for your bot, see /help for a list of commands.
                    By the way, when you've finished creating your cool bot, ping our Bot Support if you want a better username for it.
                    Just make sure the bot is fully operational before you do this.
                    
                    Use this token to access the HTTP API:
                    <token>
                    Keep your token secure and store it safely, it can be used by anyone to control your bot.
                    
                    For a description of the Bot API, see this page: https://core.telegram.org/bots/api
                    """;
        } else if (this == ASK_BOT_NAME) {
            return """
                    Please paste the name you provided to BotFather after the message:
                    
                    Alright, a new bot. How are we going to call it? Please choose a name for your bot.
                    """;
        } else if (this == ASK_WELCOME_MESSAGE) {
            return """
                    Please provide any sentence that you like.
                    """;
        } else if (this == ASK_WORKING_HOURS) {
            return """
                     Example:
                     Monday: 09:30 - 17:00
                     Tuesday: 09:00 - 17:00
                     Wednesday: 09:00 - 16:00, 17:00 - 20:00
                     Thursday: 09:00 - 17:00
                     Friday: 10:00 - 14:00
                     Saturday: None
                     Sunday: None
                    """;
        } else if (this == ASK_JOBS) {
            return """
                     Example:
                     Men's haircut: 00:30
                     Women's haircut: 01:30
                     Lesson: 01:00, 02:00
                     Yoga class: 01:00
                    """;
        }
        return "";
    }

    public static Optional<BotCreationState> fromString(String stateName) {
        return Arrays.stream(BotCreationState.values())
                .filter(state -> state.name().equalsIgnoreCase(stateName))
                .findFirst();
    }
}
