package org.example.bots.manager.actions.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bots.manager.actions.helpers.BotsCommandHelper;
import org.example.bots.manager.actions.helpers.CommonCommandHelper;
import org.example.bots.manager.actions.helpers.ScheduleCommandHelper;
import org.example.bots.manager.constants.Callback;
import org.example.client.api.controller.AppointmentApi;
import org.example.client.api.controller.BotApi;
import org.example.client.api.processor.MessageBatchProcessor;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.example.telegram.components.inline.keyboard.ButtonsGenerator;
import org.example.telegram.components.inline.keyboard.CalendarKeyboardGenerator;
import org.example.telegram.components.inline.keyboard.MessageGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduleSlashCommand implements ISlashCommand {
    private final MessageBatchProcessor messageBatchProcessor;
    private final CommonCommandHelper commonCommandHelper;
    private final BotsCommandHelper botsCommandHelper;
    private final ScheduleCommandHelper scheduleCommandHelper;
    private final BotApi botApi;
    private final AppointmentApi appointmentApi;

    @Override
    public void execute(Message message) {
        Long userId = message.getFrom().getId();
        if (!commonCommandHelper.botsExist(userId)) {
            String text = """
                    👋 Welcome to the Appointment Manager!
                    You don't have any bots created.
                    You need to create a new bot using the /create command.
                    Type any text to return to the menu.""";
            messageBatchProcessor.addMessage(
                    SendMessage.builder()
                            .chatId(userId)
                            .text(text)
                            .build()
            );
            return;
        }
        botsCommandHelper.showBotsList(message.getFrom().getId(), true, null);
    }

    public void processCallbackResponse(Update update) {
        String callback = update.getCallbackQuery().getData();
        Message message = update.getCallbackQuery().getMessage();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = message.getChatId();
        if (!callback.contains(Callback.DELIMITER_SCHEDULE_STATE)) return;

        String callbackState = callback.split(Callback.DELIMITER_SCHEDULE_STATE)[0] + Callback.DELIMITER_SCHEDULE_STATE;
        String data = callback.split(Callback.DELIMITER_SCHEDULE_STATE)[1];
        switch (callbackState) {
            case Callback.SELECT_APPOINTMENTS_BOT, Callback.UPDATE_DATE, Callback.BACK_TO_DATES: {
                sendDatesSelection(data, chatId, message.getMessageId());
                break;
            }
            case Callback.BACK_TO_APPOINTMENTS_BOT: {
                botsCommandHelper.showBotsList(userId, true, message.getMessageId());
                break;
            }
            case Callback.SELECT_DATE, Callback.BACK_TO_APPOINTMENTS: {
                sendAppointmentsList(data, chatId, message.getMessageId());
                break;
            }
            case Callback.SELECT_APPOINTMENT, Callback.BACK_TO_APPOINTMENT: {
                sendAppointmentsManageOptions(data, chatId, message.getMessageId());
                break;
            }
            case Callback.DISPLAY_CLIENT_DETAILS: {
                sendClientDetails(data, chatId, message.getMessageId());
                break;
            }
            default:
        }
    }

    private void sendDatesSelection(String data, Long chatId, Integer messageId) {
        boolean isUpdate = data.contains(Callback.DELIMITER_SCHEDULE_STATE_DATES);
        String botId = isUpdate ? data.split(Callback.DELIMITER_SCHEDULE_STATE_DATES)[1] : data;
        boolean isUpdateDate = data.split(Callback.DELIMITER_SCHEDULE_STATE_DATES).length > 2;
        Bot bot = botApi.getBot(botId);
        int year = isUpdate && isUpdateDate ? Integer.parseInt(data.split(Callback.DELIMITER_SCHEDULE_STATE_DATES)[2].split("-")[0]) : LocalDate.now().getYear();
        int month = isUpdate && isUpdateDate ? Integer.parseInt(data.split(Callback.DELIMITER_SCHEDULE_STATE_DATES)[2].split("-")[1]) : LocalDate.now().getMonthValue();
        String suffix = Callback.DELIMITER_SCHEDULE_STATE_DATES + botId + Callback.DELIMITER_SCHEDULE_STATE_DATES;
        InlineKeyboardMarkup calendar = CalendarKeyboardGenerator.generateCalendar(year, month, bot.getWorkingHours(),
                Callback.SELECT_DATE + suffix, Callback.UPDATE_DATE + suffix,
                "<< Back To Bots", Callback.BACK_TO_APPOINTMENTS_BOT + Callback.DELIMITER_SCHEDULE_STATE_DATES);
        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(chatId.toString(), "📅 Please select a date:", calendar, messageId));
    }

    private void sendAppointmentsList(String data, Long chatId, Integer messageId) {
        String[] parts = data.split(Callback.DELIMITER_SCHEDULE_STATE_DATES);
        Long botId = Long.parseLong(parts[1]);
        String date = parts[2];
        String text = "Here it is: " + date + ".\n\nPlease select the appointment you would like to manage:";
        List<Appointment> appointments = Optional.ofNullable(botApi.findAppointmentsByDate(botId, date))
                .map(List::of)
                .orElse(List.of());
        InlineKeyboardMarkup appointmentsKeyboard = scheduleCommandHelper.appointmentsList(appointments, date, parts[1]);
        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), text, appointmentsKeyboard, messageId
        ));
    }

    private void sendAppointmentsManageOptions(String data, Long chatId, Integer messageId) {
        String[] parts = data.split(Callback.DELIMITER_SCHEDULE_STATE_DATES);
        String appointmentId = parts[0];
        String date = parts[1];
        String botId = parts[2];
        Appointment appointment = appointmentApi.getAppointment(appointmentId);
        Appointment.AppointmentStatus appointmentStatus = appointment.getStatus();
        String text = String.format("Here it is: %s (%s), 🕒 Date: %s at %s, Status: %s\n\n📝 What would you like to do with this appointment?",
                appointment.getJob().getType(), appointment.getJob().getDuration() + "h",
                date, appointment.getAppointmentDate().toLocalTime(), appointmentStatus);
        InlineKeyboardMarkup markup = scheduleCommandHelper.appointmentOptions(appointmentStatus, appointmentId, date, botId);
        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), text, markup, messageId
        ));
    }

    private void sendClientDetails(String data, Long chatId, Integer messageId) {
        String[] parts = data.split(Callback.DELIMITER_SCHEDULE_STATE_DATES);
        String appointmentId = parts[0];
        Client client = appointmentApi.getClient(Long.parseLong(appointmentId));
        String text = String.format("Client Details:\n\n👤 Name: %s\n📞 Phone: %s\n📧 Email: %s",
                client.getName(), client.getPhoneNumber(), client.getEmail());
        String[][] buttonConfig = {
                {"<< Back To Appointment:" + Callback.BACK_TO_APPOINTMENT + data}
        };
        List<List<InlineKeyboardButton>> keyboard = ButtonsGenerator.createKeyboard(buttonConfig);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        messageBatchProcessor.addTextUpdate(MessageGenerator.createEditMessageWithMarkup(
                chatId.toString(), text, markup, messageId
        ));
    }
}
