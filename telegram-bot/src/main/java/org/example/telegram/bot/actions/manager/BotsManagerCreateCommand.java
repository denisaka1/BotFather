package org.example.telegram.bot.actions.manager;

import lombok.extern.slf4j.Slf4j;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.telegram.bot.services.dynamic.RegistrationService;
import org.example.telegram.components.forms.FormStep;
import org.example.telegram.components.forms.GenericForm;
import org.example.telegram.components.validators.BotMessageValidator;
import org.example.telegram.components.validators.StringValidator;
import org.example.telegram.components.validators.WorkingDurationsValidator;
import org.example.telegram.components.validators.WorkingHoursValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.example.telegram.bot.utils.MessageExtractor.*;

@Slf4j
@Component
public class BotsManagerCreateCommand extends AbstractBotCommand {
    private final RegistrationService botsRegistryService;

    public BotsManagerCreateCommand(ApiRequestHelper apiRequestHelper, RegistrationService botsRegistryService) {
        super(apiRequestHelper);
        this.botsRegistryService = botsRegistryService;
        userForm = createForm();
    }

    @Override
    public String execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!checkIfUserExists(userId)) {
            forceCompleted = true;
            return """
                    👋 Welcome to the Bots Creator!
                    You need to register using the /start command to create a new bot.
                    Type any text to return to the menu.""";
        }

        String response = userForm.handleResponse(message.getText());
        if (userForm.isCompleted()) {
            buildAndSaveBot(userForm.getUserResponses(), userId);
        }
        return response;
    }

    @Override
    public boolean isCompleted() {
        return userForm.isCompleted() || forceCompleted;
    }

    private GenericForm createForm() {
        String firstMessage = """
                👋 Welcome to the Bots Creator!
                
                Please follow the steps to create a new bot (Better to do it on a PC):
                
                1️⃣ Search for BotFather in Telegram and open it.
                2️⃣ Send the command /newbot.
                3️⃣ Follow the instructions to choose a name and username for your bot.
                4️⃣ Copy the final message containing your bot token.
                """;
        String workingHoursMessage = """
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
        return new GenericForm(Arrays.asList(
                new FormStep<>("📩 Please paste the last message you received from BotFather.", new BotMessageValidator(), "❌ Invalid bot creation message! Please try again.", "✅ Bot creation message is verified!", "forwardedMessage"),
                new FormStep<>("📝 What is your bot name?", new StringValidator(), "❌ Invalid name! Please enter a valid text.", "✅ Bot name saved successfully!", "name"),
                new FormStep<>("💬 What should be your bot's welcome message?", new StringValidator(), "❌ Invalid welcome message! Please enter a valid text.", "✅ Welcome message saved successfully!", "welcomeMessage"),
                new FormStep<>(workingHoursMessage, new WorkingHoursValidator(), "❌ Invalid working hours! Please try again...", "✅ Working hours are saved.", "workingHours"),
                new FormStep<>(workingDurationsMessage, new WorkingDurationsValidator(), "❌ Invalid working durations! Please try again...", "✅ Working durations are saved.", "workingDurations")
        ), firstMessage, "🎉 Your new bot has been created successfully!\nYou can now access it using the link from the first message.\n\n🙏 Thank you for creating new bot with us! Type any text to continue.");
    }

    private void buildAndSaveBot(Map<String, String> userResponses, Long userId) {
        String[] botInfo = extractBotInfoFromForwardedMsg(userResponses.get("forwardedMessage"));
        String username = botInfo[0];
        String token = botInfo[1];
        Bot bot = Bot.builder()
                .username(username)
                .token(token)
                .name(userResponses.get("name"))
                .welcomeMessage(userResponses.get("welcomeMessage"))
                .build();
        Bot savedBot = apiRequestHelper.post(
                "http://localhost:8080/api/business_owner/" + userId,
                bot,
                Bot.class
        );
        buildAndSaveWorkingHours(userResponses.get("workingHours"), savedBot);
        buildAndSaveJobs(userResponses.get("workingDurations"), savedBot);
        Bot updatedBot = apiRequestHelper.get(
                "http://localhost:8080/api/bots/" + savedBot.getId().toString(),
                Bot.class
        );
        botsRegistryService.registerBot(updatedBot);
        log.info("Bot {} created and registered successfully!", savedBot.getName());
    }

    private void buildAndSaveWorkingHours(String workingHoursStr, Bot savedBot) {
        List<WorkingHours> workingHours = extractWorkingHours(workingHoursStr, savedBot);
        for (WorkingHours workingHour : workingHours) {
            apiRequestHelper.post(
                    "http://localhost:8080/api/bots/" + savedBot.getId().toString() + "/working_hour",
                    workingHour,
                    WorkingHours.class
            );
        }
    }

    private void buildAndSaveJobs(String workingDurationsStr, Bot savedBot) {
        List<Job> jobs = extractJobs(workingDurationsStr, savedBot);
        for (Job job : jobs) {
            apiRequestHelper.post(
                    "http://localhost:8080/api/bots/" + savedBot.getId().toString() + "/job",
                    job,
                    Job.class
            );
        }
    }
}

