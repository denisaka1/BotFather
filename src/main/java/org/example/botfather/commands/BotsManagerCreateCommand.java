package org.example.botfather.commands;
import org.example.botfather.telegramform.FormStep;
import org.example.botfather.telegramform.GenericForm;
import org.example.botfather.telegramform.Validators;
import org.example.botfather.utils.ApiRequestHelper;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class BotsManagerCreateCommand implements BotCommand {
    private final GenericForm userForm;
    private final ApiRequestHelper apiRequestHelper;
    private boolean forceCompleted = false;

    public BotsManagerCreateCommand(ApiRequestHelper apiRequestHelper) {
        this.apiRequestHelper = apiRequestHelper;
        String firstMessage = """
                👋 Welcome to the Bots Creator!
                Please follow the steps to create a new bot (Better to do it on a desktop):
                1️⃣ Search for BotFather in Telegram and open it.
                2️⃣ Send the command /newbot.
                3️⃣ Follow the instructions to choose a name and username for your bot.
                4️⃣ Copy the final message containing your bot token.
                """;
        String workingHoursMessage = """
                ⏳ What are your Working hours?
                Please provide list of days in this format:
                Day: HH:MM - HH:MM (24-hour clock) OR None (if you don't work on that day)

                Example:
                Monday: 09:00 - 17:00
                Tuesday: 09:00 - 17:00
                Wednesday: 09:00 - 16:00, 17:00 - 20:00
                Thursday: 09:00 - 17:00
                Friday: 10:00 - 14:00
                Saturday: None
                Sunday: None
               """;
        String workingDurationsMessage = """
                📋 What are your working durations?
                Please provide a list of services with their respective durations in the following format:
                Service Name: HH:MM (or multiple time slots separated by commas)

                Example:
                Men's haircut: 00:30
                Women's haircut: 01:30
                Lesson: 01:00, 02:00
                Yoga class: 01:00
               """;
        userForm = new GenericForm(Arrays.asList(
                new FormStep<>("📩 Please paste the last message you received from BotFather.", new Validators.BotMessageValidator(), "❌ Invalid bot creation message! Please try again.", "✅ Bot creation message is verified!", "forwardedMessage"),
                new FormStep<>("📝 What is your bot name?", new Validators.StringValidator(), "❌ Invalid name! Please enter a valid text.", "✅ Bot name saved successfully!", "name"),
                new FormStep<>("💬 What should be your bot's welcome message?", new Validators.StringValidator(), "❌ Invalid welcome message! Please enter a valid text.", "✅ Welcome message saved successfully!", "welcomeMessage"),
                new FormStep<>(workingHoursMessage, new Validators.WorkingHoursValidator(), "❌ Invalid working hours! Please try again...", "✅ Working hours are saved.", "workingHours"),
                new FormStep<>(workingDurationsMessage, new Validators.WorkingDurationsValidator(), "❌ Invalid working durations! Please try again...", "✅ Working durations are saved.", "workingDurations")
        ), firstMessage, "🎉 Thank you for creating new bot with us! Type any text to continue.");
    }

    public boolean checkIfUserExists(Long userId) {
        return this.apiRequestHelper.get(
                "http://localhost:8080/api/business_owner/exists",
                Boolean.class,
                Map.of("userTelegramId", userId.toString())
        );
    }

    @Override
    public String execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!checkIfUserExists(userId)) {
            this.forceCompleted = true;
            return """
                    👋 Welcome to the Bots Creator!
                    You need to register using the /start command to create a new bot.
                    Type any text to return to the menu.""";
        }
        String response = userForm.handleResponse(message.getText().toLowerCase());
        if (userForm.isCompleted()) {
            // Save the bot to the database and extract the required data
            System.out.println(userForm.getUserResponses());
        }
        return response;
    }

    @Override
    public boolean isCompleted() {
        return userForm.isCompleted() || this.forceCompleted;
    }
}

