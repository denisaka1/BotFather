package org.example.data.layer.entities;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public enum BotCreationState {
    ASK_BOT_FATHER_BOT_CREATION_MESSAGE("""
                👋 Welcome to the Bots Creator!
                
                Please follow the steps to create a new bot (Better to do it on a PC):
                
                1️⃣ Search for BotFather in Telegram and open it.
                2️⃣ Send the command /newbot.
                3️⃣ Follow the instructions to choose a name and username for your bot.
                4️⃣ Copy the final message containing your bot token.
                """),
    ASK_BOT_NAME("📝 What is your bot name?"),
    ASK_WELCOME_MESSAGE("💬 What should be your bot's welcome message?"),
    ASK_WORKING_HOURS("⏳ What are your working hours?"),
    ASK_JOBS("📋 What are your working durations?"),
    COMPLETED("🎉 Your new bot has been created successfully!\nYou can now access it using the link from the first message.\n\n🙏 Thank you for creating new bot with us! Type any text to continue.");

    private final String message;

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public String getMessage() {
        return message;
    }

    public Optional<BotCreationState> getNextState() {
        return switch (this) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> Optional.of(ASK_BOT_NAME);
            case ASK_BOT_NAME -> Optional.of(ASK_WELCOME_MESSAGE);
            case ASK_WELCOME_MESSAGE -> Optional.of(ASK_WORKING_HOURS);
            case ASK_WORKING_HOURS -> Optional.of(ASK_JOBS);
            case ASK_JOBS -> Optional.of(COMPLETED);
            case COMPLETED -> Optional.empty();
        };
    }

    public Optional<BotCreationState> getPreviousState() {
        return switch (this) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE, COMPLETED -> Optional.empty();
            case ASK_BOT_NAME -> Optional.of(ASK_BOT_FATHER_BOT_CREATION_MESSAGE);
            case ASK_WELCOME_MESSAGE -> Optional.of(ASK_BOT_NAME);
            case ASK_WORKING_HOURS -> Optional.of(ASK_WELCOME_MESSAGE);
            case ASK_JOBS -> Optional.of(ASK_WORKING_HOURS);
        };
    }

    public static Optional<BotCreationState> fromString(String stateName) {
        return Arrays.stream(BotCreationState.values())
                .filter(state -> state.name().equalsIgnoreCase(stateName))
                .findFirst();
    }
}
