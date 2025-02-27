package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ClientApi {

    private static final String BASE_URL = "http://localhost:8080/api/client";

    private final ApiRequestHelper apiRequestHelper;

    public Client getClient(Long userId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + userId,
                Client.class
        );
    }

    public Appointment[] findAppointments(Long userId, Long botId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + userId + "/appointments" + "?botId=" + botId,
                Appointment[].class
        );
    }

    public Appointment[] findAppointmentsByDate(Long userId, Long botId, String date) {
        return apiRequestHelper.get(
                BASE_URL + "/" + userId + "/appointments_by_date" + "?botId=" + botId + "&date=" + date,
                Appointment[].class
        );
    }

    public Client createClient(Client client) {
        return apiRequestHelper.post(
                BASE_URL,
                client,
                Client.class
        );
    }

    public Appointment createAppointment(Appointment appointment, Long clientId, Long botId, Long jobId) {
        String url = BASE_URL + "/" + clientId + "/appointment" + "?botId=" + botId + "&jobId=" + jobId;
        return apiRequestHelper.post(url, appointment, Appointment.class);
    }

    public Appointment deleteAppointment(String appointmentId, String userTelegramId) {
        String url = BASE_URL + "/" + userTelegramId + "/appointment/" + appointmentId;
        return apiRequestHelper.put(
                url,
                null,
                Appointment.class
        );
    }

    public Client updateClient(Client client, String userTelegramId) {
        String url = BASE_URL + "/" + userTelegramId;
        return apiRequestHelper.put(
                url,
                client,
                Client.class
        );
    }
}
