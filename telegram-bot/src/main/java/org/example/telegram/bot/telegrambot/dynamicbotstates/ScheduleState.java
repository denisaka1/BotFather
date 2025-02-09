package org.example.telegram.bot.telegrambot.dynamicbotstates;
import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Job;
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
import java.util.List;

@AllArgsConstructor
public class ScheduleState implements IDynamicBotState {
    private final ApiRequestHelper apiRequestHelper;
    private static final int HOUR_DISPLAY_RANGE = 4; // Shows 2 hours at a time
    private static final int WORKING_HOURS_START = 8;  // Earliest selectable hour
    private static final int WORKING_HOURS_END = 17;   // Latest selectable hour

    @Override
    public BotApiMethod<?> handle(DynamicBotsMessageHandler context, Bot bot, Message message, CallbackQuery callbackData) {
        Long chatId = message.getChatId();

        if (callbackData != null) {
            return handleCallbackQuery(context, bot, message, callbackData.getData());
        } else if (message.hasText() && !message.getFrom().getIsBot()) { // In case a text message was received instead of a callback
            return null;
        }

        return sendCalendar(chatId, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message);
    }

    private List<Job> fetchBotJobs(Bot bot) {
        return List.of(apiRequestHelper.get("http://localhost:8080/api/bots/" + bot.getId() + "/jobs", Job[].class));
    }

    private BotApiMethod<?> handleCallbackQuery(DynamicBotsMessageHandler context, Bot bot, Message message, String callbackData) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        if (callbackData.startsWith("date:")) {
            String selectedDate = callbackData.split(":")[1];
            return sendHour(chatId, messageId, selectedDate);
        } else if (callbackData.startsWith("month:")) {
            String[] parts = callbackData.split(":")[1].split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month);
            return MessageGenerator.createEditMessageReplyMarkup(chatId.toString(), messageId, calendar);
        } else if (callbackData.startsWith("hour:")) {
            return handleHourSelection(chatId, messageId, callbackData);
        } else if (callbackData.startsWith("selectedTime:")) {
            String[] parts = callbackData.split(":");
            if (parts.length < 3) {
                return new SendMessage(chatId.toString(), "⚠ Invalid time selection!");
            }
            String selectedDate = parts[1];
            String selectedTime = parts[2] + ":" + parts[3];
            return askForJobSelection(chatId, messageId, bot, selectedDate, selectedTime);
        } else if ("backToDates".equals(callbackData)) {
            return sendCalendar(chatId, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message);
        } else if (callbackData.startsWith("backToTimeSelection")) {
            String selectedDate = callbackData.split("@")[1];
            return sendHour(chatId, messageId, selectedDate);
        } else if (callbackData.startsWith("jobSelected")) {
            String[] parts = callbackData.split("@");
            String[] jobParts = parts[1].split(":");
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
        return null;
    }

    private BotApiMethod<?> sendHour(Long chatId, Integer messageId, String selectedDate) {
        InlineKeyboardMarkup hourSelector = HourKeyboardGenerator.generateHourKeyboard(selectedDate, WORKING_HOURS_START, WORKING_HOURS_START + HOUR_DISPLAY_RANGE, WORKING_HOURS_START, WORKING_HOURS_END, HOUR_DISPLAY_RANGE);
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "📅 You selected: " + selectedDate + "\n\n⏳ Please select a time:", hourSelector, messageId);
    }

    private BotApiMethod<?> sendCalendar(Long chatId, int year, int month, Message message) {
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "Please select a date:", CalendarKeyboardGenerator.generateCalendar(year, month), message.getMessageId());
    }

    private BotApiMethod<?> handleHourSelection(Long chatId, Integer messageId, String callbackData) {
        String[] parts = callbackData.split(":");

        if (parts.length < 4) {
            return new SendMessage(chatId.toString(), "⚠ Invalid hour selection!");
        }
        String selectedDate = parts[1];
        int startHour = Integer.parseInt(parts[2]);
        int endHour = Integer.parseInt(parts[3]);
        InlineKeyboardMarkup hourSelector = HourKeyboardGenerator.generateHourKeyboard(selectedDate, startHour, endHour, WORKING_HOURS_START, WORKING_HOURS_END, HOUR_DISPLAY_RANGE);
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "📅 You selected: " + selectedDate + "\n\n⏳ Choose a time:", hourSelector, messageId);
    }

    private BotApiMethod<?> askForJobSelection(Long chatId, Integer messageId, Bot bot, String selectedDate, String selectedTime) {
        List<Job> jobs = fetchBotJobs(bot);
        InlineKeyboardMarkup jobKeyboard = JobKeyboardBuilder.createJobSelectionKeyboard(jobs, selectedDate, selectedTime);
        return MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "✅ You selected an appointment on " + selectedDate + " at " + selectedTime + "\n\n🛠 Please choose a service:", jobKeyboard, messageId);
    }
}
