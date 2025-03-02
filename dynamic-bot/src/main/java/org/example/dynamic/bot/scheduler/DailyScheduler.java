package org.example.dynamic.bot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.api.controller.ClientApi;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.example.mail.service.services.MailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyScheduler {
    private final MailService mailService;
    private final ClientApi clientApi;

    @Scheduled(cron = "0 0 9 * * ?")  // Runs every day at 09:00 AM UTC
    public void sendDailyEmails() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        log.info("📧 Starting daily email job at {}", now);
        List<Client> clients = List.of(clientApi.findAllClients());
        int emailsSent = 0;
        for (Client client : clients) {
            String to = client.getEmail();
            List<Appointment> appointments = client.getAppointments().stream()
                    .filter(appointment -> isAppointmentToday(appointment, now, tomorrow) && appointment.getStatus() == Appointment.AppointmentStatus.APPROVED)
                    .toList();
            if (appointments.isEmpty()) continue;
            String subject = "📅 Daily Appointments Reminder from Bots Manager";
            String message = buildEmailContent(appointments);
            mailService.sendEmail(to, subject, message, true);
            emailsSent++;
        }

        log.info("✅ Finished daily email job. Emails sent: {}", emailsSent);
    }

    private boolean isAppointmentToday(Appointment appointment, LocalDateTime now, LocalDateTime tomorrow) {
        LocalDateTime appointmentDate = appointment.getAppointmentDate();
        return !appointmentDate.isBefore(now) && appointmentDate.isBefore(tomorrow);
    }

    private String buildEmailContent(List<Appointment> todaysAppointments) {
        StringJoiner appointmentList = new StringJoiner("", "<ul>", "</ul>");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        todaysAppointments.forEach(appointment ->
                appointmentList.add("<li>")
                        .add(appointment.getJob().getType())
                        .add(" (").add(String.valueOf(appointment.getJob().getDuration()))
                        .add("h) at ")
                        .add(appointment.getAppointmentDate().toLocalTime().format(timeFormatter))
                        .add("</li>"));

        return String.format("""
                <h3>Hello!</h3>
                <p>This is your daily appointments update.</p>
                <p>Here are your appointments for today:</p>
                %s
                """, appointmentList);
    }
}
