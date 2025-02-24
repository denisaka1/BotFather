package org.example.backend.api.data.services;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.AppointmentRepository;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public Appointment updateAppointment(Long id, Appointment appointment) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isPresent()) {
            Appointment currentAppointment = optionalAppointment.get();
            currentAppointment.setAppointmentDate(appointment.getAppointmentDate());
            currentAppointment.setStatus(appointment.getStatus());
            currentAppointment.setJob(appointment.getJob());
            return appointmentRepository.save(currentAppointment);
        } else {
            return appointmentRepository.save(appointment);
        }
    }

    public Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }

    public Client getClient(Long id) {
        Appointment appointment = getAppointment(id);
        return appointment.getClient();
    }
}
