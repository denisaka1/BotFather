package org.example.telegram.bot.telegrambot.dynamicbotstates;
import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.example.telegram.bot.telegrambot.DynamicBotsMessageHandler;
import org.example.telegram.components.inline.keyboard.CalendarKeyboardGenerator;
import org.example.telegram.components.inline.keyboard.HourKeyboardGenerator;
import org.example.telegram.components.inline.keyboard.JobKeyboardBuilder;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
public class ScheduleState implements IDynamicBotState {
    private final ApiRequestHelper apiRequestHelper;

    @Override
    public BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData) {
        if (callbackData != null) {
            return handleCallbackQuery(bot, message, callbackData.getData());
        }

        if (message.hasText() && !message.getFrom().getIsBot()) {
            return null;
        }

        return sendCalendar(message.getChatId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot.getWorkingHours());
    }

    private List<Job> fetchBotJobs(Bot bot) {
        return List.of(apiRequestHelper.get("http://localhost:8080/api/bots/" + bot.getId() + "/jobs", Job[].class));
    }

    private BotApiMethod<?> handleCallbackQuery(Bot bot, Message message, String callbackData) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        if (callbackData.startsWith("date:")) {
            return sendHourSelection(chatId, messageId, callbackData.split(":")[1], bot.getWorkingHours());
        }
        if (callbackData.startsWith("month:")) {
            return updateCalendar(chatId, messageId, callbackData, bot.getWorkingHours());
        }
        if (callbackData.startsWith("hour:")) {
            return updateHourSelection(chatId, messageId, callbackData, bot.getWorkingHours());
        }
        if (callbackData.startsWith("selectedTime:")) {
            return processTimeSelection(chatId, messageId, bot, callbackData);
        }
        if (callbackData.equals("backToDates")) {
            return sendCalendar(chatId, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot.getWorkingHours());
        }
        if (callbackData.startsWith("backToTimeSelection")) {
            return sendHourSelection(chatId, messageId, callbackData.split("@")[1], bot.getWorkingHours());
        }
        if (callbackData.startsWith("jobSelected")) {
            return confirmJobSelection(chatId, callbackData);
        }

        return null;
    }

    private BotApiMethod<?> sendHourSelection(Long chatId, Integer messageId, String selectedDate, List<WorkingHours> workingHours) {
        InlineKeyboardMarkup hourKeyboard = HourKeyboardGenerator.generateHourKeyboard(
                selectedDate, null, workingHours
        );

        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "📅 You selected: " + selectedDate + "\n\n⏳ Please select a time:",
                hourKeyboard, messageId
        );
    }

    private BotApiMethod<?> sendCalendar(Long chatId, int year, int month, Message message, List<WorkingHours> workingHours) {
        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, workingHours);
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "📅 Please select a date:", calendar, message.getMessageId());
    }

    private BotApiMethod<?> updateCalendar(Long chatId, Integer messageId, String callbackData, List<WorkingHours> workingHours) {
        String[] parts = callbackData.split(":")[1].split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, workingHours);
        return MessageGenerator.createEditMessageReplyMarkup(chatId.toString(), messageId, calendar);
    }

    private BotApiMethod<?> updateHourSelection(Long chatId, Integer messageId, String callbackData, List<WorkingHours> workingHours) {
        String[] parts = callbackData.split(":");
        if (parts.length < 4) {
            return new SendMessage(chatId.toString(), "⚠ Invalid hour selection!");
        }
        String selectedDate = parts[1];
        String startHour = String.format("%02d:00", Integer.parseInt(parts[2]));

        InlineKeyboardMarkup hourKeyboard = HourKeyboardGenerator.generateHourKeyboard(
                selectedDate, startHour, workingHours
        );

        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "📅 You selected: " + selectedDate + "\n\n⏳ Choose a time:",
                hourKeyboard, messageId
        );
    }

    private BotApiMethod<?> processTimeSelection(Long chatId, Integer messageId, Bot bot, String callbackData) {
        String[] parts = callbackData.split(":");

        if (parts.length < 3) {
            return new SendMessage(chatId.toString(), "⚠ Invalid time selection!");
        }

        String selectedDate = parts[1];
        String selectedTime = parts[2] + ":" + parts[3];

        return requestJobSelection(chatId, messageId, bot, selectedDate, selectedTime);
    }

    private BotApiMethod<?> requestJobSelection(Long chatId, Integer messageId, Bot bot, String selectedDate, String selectedTime) {
        List<Job> jobs = fetchBotJobs(bot);

        InlineKeyboardMarkup jobKeyboard = JobKeyboardBuilder.createJobSelectionKeyboard(jobs, selectedDate, selectedTime);
        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), "✅ You selected an appointment on " + selectedDate + " at " + selectedTime + "\n\n🛠 Please choose a service:",
                jobKeyboard, messageId
        );
    }

    private BotApiMethod<?> confirmJobSelection(Long chatId, String callbackData) {
        String[] parts = callbackData.split("@");
        if (parts.length < 4) {
            return new SendMessage(chatId.toString(), "⚠ Invalid job selection!");
        }

        String[] jobParts = parts[1].split(":");
        if (jobParts.length < 3) {
            return new SendMessage(chatId.toString(), "⚠ Invalid job details!");
        }

        String jobId = jobParts[0];
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        String selectedDate = parts[2];
        String selectedTime = parts[3];

        return new SendMessage(
                chatId.toString(),
                "✅ Your appointment for a " + jobType + " (" + jobDuration + "h) on " + selectedDate + " at " + selectedTime + " has been confirmed.\nYou'll receive a confirmation message here shortly."
        );
    }
}
