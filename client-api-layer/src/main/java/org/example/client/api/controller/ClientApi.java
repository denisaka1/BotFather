package org.example.client.api.controller;
import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Client;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ClientApi {

    private static final String BASE_URL = "http://localhost:8080/api/client/";

    private final ApiRequestHelper apiRequestHelper;

    public Client getClient(Long userId) {
        return apiRequestHelper.get(
                BASE_URL + userId,
                Client.class
        );
    }
}
