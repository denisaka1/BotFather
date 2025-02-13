package org.example.telegram.bot.actions.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.BusinessOwnerApi;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BotCreationState;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.telegram.bot.redis.entity.BotSession;
import org.example.telegram.bot.redis.repository.BotSessionRepository;
import org.example.telegram.bot.redis.service.BotSessionService;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.telegram.bot.utils.MessageExtractor.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateSlashCommand implements ISlashCommand {
    private final BotApi botApi;
    private final BusinessOwnerApi businessOwnerApi;
    private final RegistrationService botsRegistryService;
    private final BotSessionRepository botSessionRepository;
    private final BotSessionService botSessionService;

    private GenericForm userForm;
    private Long userId;
    private String userInput;
    private BotSession botSession;
    private Bot bot;

    @Override
    public String execute(Message message) {
        userId = message.getFrom().getId();
        userInput = message.getText();
//        botSession = botSessionService.getBotSession(chatId);

        bot = businessOwnerApi.createBotIfNotPresent(userId);

        return bot.getCreationState().getMessage();
    }

    public boolean isCompleted() {
        return bot.getCreationState().isCompleted();
    }

    public String processUserResponse(Message message) {
//        BotCreationState currentState = botSession.getCreationState();
        userInput = message.getText();
        BotCreationState currentState = bot.getCreationState();

        if (!isValidInput(currentState, userInput)) {
            return "❌ Invalid input!\n" + currentState.getExampleMessage();
        }

        AtomicReference<String> response = new AtomicReference<>("");
        currentState.getNextState().ifPresentOrElse(nextState -> {
            switch (currentState) {
                case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> setTokenAndUsername(userInput);
                case ASK_BOT_NAME -> bot.setName(userInput);
                case ASK_WELCOME_MESSAGE -> bot.setWelcomeMessage(userInput);
                case ASK_WORKING_HOURS -> buildAndSaveWorkingHours(userInput);
                case ASK_JOBS -> buildAndSaveJobs(userInput);
                case COMPLETED -> botsRegistryService.registerBot(bot);
            }
            bot.setCreationState(nextState);
            bot = botApi.updateBot(bot.getId(), bot);
            response.set(nextState.getMessage());
        }, () -> {
            // If no next state, user has completed registration
//            botSessionService.finalizeBotSession(userId, botSession);
            businessOwnerApi.addBot(userId, bot);
            response.set("🎉 Your new bot has been created successfully!\nYou can now access it using the link from the first message.\n\n🙏 Thank you for creating new bot with us! Type any text to continue.");
        });
        return response.get();
    }

    private boolean isValidInput(BotCreationState state, String userMessage) {
        return switch (state) {
            case ASK_BOT_FATHER_BOT_CREATION_MESSAGE -> new BotMessageValidator().validate(userMessage);
            case ASK_BOT_NAME, ASK_WELCOME_MESSAGE -> new StringValidator().validate(userMessage);
            case ASK_WORKING_HOURS -> new WorkingHoursValidator().validate(userMessage);
            case ASK_JOBS -> new WorkingDurationsValidator().validate(userMessage);
            default -> true;
        };
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

    private void setTokenAndUsername(String userInput) {
        bot.setToken(BotMessageValidator.extractToken(userInput));
        bot.setUsername(BotMessageValidator.extractBotLink(userInput));
    }

//    private void buildAndSaveBot(Map<String, String> userResponses, Long userId) {
//        String[] botInfo = extractBotInfoFromForwardedMsg(userResponses.get("forwardedMessage"));
//        String username = botInfo[0];
//        String token = botInfo[1];
//        Bot bot = Bot.builder()
//                .username(username)
//                .token(token)
//                .name(userResponses.get("name"))
//                .welcomeMessage(userResponses.get("welcomeMessage"))
//                .build();
//        Bot savedBot = businessOwnerApi.addBot(userId, bot);
//
//        buildAndSaveWorkingHours(userResponses.get("workingHours"), savedBot);
//        buildAndSaveJobs(userResponses.get("workingDurations"), savedBot);
//        Bot updatedBot = botApi.getBot(savedBot.getId());
//        botsRegistryService.registerBot(updatedBot);
//        log.info("Bot {} created and registered successfully!", savedBot.getName());
//    }

    private void buildAndSaveWorkingHours(String workingHoursStr) {
        List<WorkingHours> workingHours = extractWorkingHours(workingHoursStr, bot);
        for (WorkingHours workingHour : workingHours) {
            botApi.addWorkingHours(bot.getId(), workingHour);
        }
    }

    private void buildAndSaveJobs(String workingDurationsStr) {
        List<Job> jobs = extractJobs(workingDurationsStr, bot);
        for (Job job : jobs) {
            botApi.addJob(bot.getId(), job);
        }
    }
}

