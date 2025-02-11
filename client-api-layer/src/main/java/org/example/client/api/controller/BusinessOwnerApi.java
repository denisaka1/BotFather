package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BusinessOwnerApi {
    private static final String BASE_URL = "http://localhost:8080/api/business_owner/";

    private final ApiRequestHelper apiRequestHelper;

    public Bot[] getBots(Long userId) {
        return apiRequestHelper.get(
                BASE_URL + userId,
                Bot[].class
        );
    }

    public Bot addBot(Long userId, Bot bot) {
        return apiRequestHelper.post(
                BASE_URL + userId,
                bot,
                Bot.class
        );
    }

    public boolean isPresent(Long userId) {
        return apiRequestHelper.get(
                BASE_URL + userId + "/exists",
                Boolean.class
        );
    }

}
