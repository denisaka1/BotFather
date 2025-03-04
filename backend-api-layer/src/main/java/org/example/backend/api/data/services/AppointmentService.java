package org.example.backend.api.data.services;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.AppointmentRepository;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Client;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public Appointment updateAppointment(Long id, Appointment appointment) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        Appointment appToSave = optionalAppointment.map(app -> {
            app.setAppointmentDate(appointment.getAppointmentDate());
            app.setStatus(appointment.getStatus());
            app.setJob(appointment.getJob());
            return app;
        }).orElse(appointment);

        return appointmentRepository.save(appToSave);
    }

    public Optional<Appointment> getAppointment(Long id) {
        return appointmentRepository.findById(id);
    }

    public Optional<Client> getClient(Long id) {
        return getAppointment(id).map(Appointment::getClient);
    }

    public Optional<Bot> getBotFromAppointmentResult(Long telegramId) {
        return appointmentRepository.findByOwnerResponseAndClientTelegramId(telegramId)
                .map(Appointment::getBot);
    }
}
