package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class AppointmentApi {
    private final ApiRequestHelper apiRequestHelper;
    private static final String BASE_URL = "http://localhost:8080/api/appointments";

    public Appointment updateAppointment(Appointment appointment) {
        return apiRequestHelper.put(
                BASE_URL + "/" + appointment.getId(),
                appointment,
                Appointment.class
        );
    }

    public Appointment getAppointment(String id) {
        return apiRequestHelper.get(
                BASE_URL + "/" + id,
                Appointment.class
        );
    }

    public Client getClient(Long appointmentId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + appointmentId + "/client",
                Client.class
        );
    }
}
