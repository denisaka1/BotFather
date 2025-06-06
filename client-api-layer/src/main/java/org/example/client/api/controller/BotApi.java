package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BotApi {
    private final ApiRequestHelper apiRequestHelper;
    private static final String BASE_URL = "http://localhost:8080/api/bots";

    public Bot getBot(Long botId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + botId,
                Bot.class
        );
    }

    public Bot getBot(String botId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + botId,
                Bot.class
        );
    }

    public Bot[] getBots() {
        return apiRequestHelper.get(
                BASE_URL,
                Bot[].class
        );
    }

    public BusinessOwner getOwner(Long botId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + botId + "/owner",
                BusinessOwner.class
        );
    }

    public Bot updateBot(Bot bot) {
        return apiRequestHelper.put(
                BASE_URL + "/" + bot.getId(),
                bot,
                Bot.class
        );
    }

    public WorkingHours addWorkingHours(Long botId, WorkingHours workingHour) {
        return apiRequestHelper.post(
                BASE_URL + "/" + botId + "/working_hour",
                workingHour,
                WorkingHours.class
        );
    }

    public Job addJob(Long botId, Job job) {
        return apiRequestHelper.post(
                BASE_URL + "/" + botId + "/job",
                job,
                Job.class
        );
    }

    public Job[] getJobs(Long botId) {
        return apiRequestHelper.get(
                BASE_URL + "/" + botId + "/jobs",
                Job[].class
        );
    }

    public Appointment[] findAppointmentsByDate(Long botId, String date) {
        return apiRequestHelper.get(
                BASE_URL + "/" + botId + "/appointments_by_date" + "?date=" + date,
                Appointment[].class
        );
    }
}
