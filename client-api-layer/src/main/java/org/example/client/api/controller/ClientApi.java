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

    public Client createClient(Client client) {
        return apiRequestHelper.post(
                BASE_URL,
                client,
                Client.class
        );
    }

    public Appointment createAppointment(Appointment appointment, Long clientId, Long botId) {
        return apiRequestHelper.post(
                BASE_URL + "/" + clientId + "/appointment",
                appointment,
                Appointment.class
        );
    }
}
