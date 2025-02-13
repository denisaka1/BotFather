package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BusinessOwner;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BusinessOwnerApi {
    private static final String BASE_URL = "http://localhost:8080/api/business_owner";

    private final ApiRequestHelper apiRequestHelper;

    public Bot[] getBots(Long userId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + userId + "/bots",
                Bot[].class
        );
    }

    public Bot addBot(Long userId, Bot bot) {
        return apiRequestHelper.post(
                BASE_URL + "/" + userId,
                bot,
                Bot.class
        );
    }

    public BusinessOwner create(BusinessOwner businessOwner) {
        return apiRequestHelper.post(
                BASE_URL,
                businessOwner,
                BusinessOwner.class
        );
    }

    public boolean isPresent(Long userId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + userId + "/exists",
                Boolean.class
        );
    }

    public boolean isRegistered(Long userId) {
        if (!isPresent(userId)) return false;

        return getOwner(userId).getRegistrationState().isCompleted();
    }

    public BusinessOwner getOwner(Long userId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + userId,
                BusinessOwner.class
        );
    }

    public BusinessOwner update(BusinessOwner businessOwner) {
        return apiRequestHelper.put(
                BASE_URL + "/" + businessOwner.getId(),
                businessOwner,
                BusinessOwner.class
        );
    }

    public BusinessOwner createIfNotPresent(BusinessOwner businessOwner) {
        if (isPresent(businessOwner.getUserTelegramId()))
            return getOwner(businessOwner.getUserTelegramId());

        return create(businessOwner);
    }

    public Bot createBotIfNotPresent(Long userId) {
        // Get bot that has the stating creationState, can only have 1 at any given time
        // In none present, create one
        // TODO: fix it
        Bot[] botArray = getBots(userId);
        Bot bot = new Bot();
        for (Bot b : botArray) {
            if (!b.getCreationState().isCompleted())
                return b;
        }
        return addBot(userId, bot);
    }
}
