package org.example.telegram.bot.actions.dynamic;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.BotApi;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.*;
import org.example.telegram.bot.polling.BotsManager;
import org.example.telegram.bot.services.dynamic.DynamicMessageService;
import org.example.telegram.components.inline.keyboard.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduleState implements IDynamicBotState {
    private final BotsManager botsManager;
    private final BotApi botApi;
    private final ClientApi clientApi;

    @Override
    public BotApiMethod<?> handle(DynamicMessageService context, Bot bot, Message message, CallbackQuery callbackData) {
        if (callbackData != null) {
            return handleCallbackQuery(bot, message, callbackData, context);
        }

        if (message.hasText() && !message.getFrom().getIsBot()) {
            return null;
        }

        return sendCalendar(message.getChatId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue(), message, bot.getWorkingHours());
    }

    private List<Job> fetchBotJobs(Bot bot) {
        return List.of(botApi.getJobs(bot.getId()));
    }

    private BotApiMethod<?> handleCallbackQuery(Bot bot, Message message, CallbackQuery callback, DynamicMessageService context) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String callbackData = callback.getData();

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
            return confirmJobSelection(chatId, callbackData, messageId, bot);
        }
        if ("BackToMenu".equals(callbackData)) {
            ScheduleOrCancelQuestionState scheduleOrCancelQuestionState = context.getScheduleOrCancelQuestionState();
            context.setState(callback.getFrom().getId(), scheduleOrCancelQuestionState);
            return scheduleOrCancelQuestionState.handle(context, bot, message, callback);
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

    private void saveAppointment(String selectedDate, String selectedTime, Long chatId, Bot bot, String jobId, String jobType, String jobDuration) {
        Client client = clientApi.getClient(chatId);
        LocalDateTime selectedDateTime = LocalDateTime.of(
                LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                LocalTime.parse(selectedTime, DateTimeFormatter.ofPattern("HH:mm"))
        );
        Appointment appointment = Appointment.
                builder().
                appointmentDate(selectedDateTime).
                status(Appointment.AppointmentStatus.PENDING).
                build();
        Appointment savedAppointment = clientApi.createAppointment(appointment, client.getId(), bot.getId(), Long.parseLong(jobId));
        BusinessOwner botOwner = botApi.getOwner(bot.getId());
        String confirmationMessage = "Hi " + botOwner.getFirstName() + " 👋\nA new " + jobType + " (" + jobDuration + "h) appointment has been scheduled for "
                + selectedDate + " at " + selectedTime + ".\nWhat would you like to do?";
        String[][] buttonConfig = {
                {"CONFIRM ✅:confirmAppointment" + savedAppointment.getId(), "DECLINE ❌:declineAppointment" + savedAppointment.getId()}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        SendMessage confirmMsg =  MessageGenerator.createSendMessageWithMarkup(
                botOwner.getUserTelegramId().toString(), confirmationMessage,
                markup
        );
        botsManager.sendMessage(confirmMsg);
    }

    private BotApiMethod<?> confirmJobSelection(Long chatId, String callbackData, Integer messageId, Bot bot) {
        String[] parts = callbackData.split("@");
        if (parts.length < 4) {
            return new SendMessage(chatId.toString(), "⚠ Invalid service selection!");
        }

        String[] jobParts = parts[1].split(":");
        if (jobParts.length < 3) {
            return new SendMessage(chatId.toString(), "⚠ Invalid service details!");
        }

        String jobId = jobParts[0];
        String jobType = jobParts[1];
        String jobDuration = jobParts[2];
        String selectedDate = parts[2];
        String selectedTime = parts[3];
        saveAppointment(selectedDate, selectedTime, chatId, bot, jobId, jobType, jobDuration);
        String returnMessage = String.format(
                "✅ Your appointment for a %s (%s h) on %s at %s is waiting for confirmation.\nYou'll receive a confirmation message here shortly.",
                jobType, jobDuration, selectedDate, selectedTime
        );
        String[][] buttonConfig = {
                {"<< Back To Menu:BackToMenu"}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), returnMessage,
                markup, messageId
        );
    }
}
