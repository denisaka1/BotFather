package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BotApi {
    private final ApiRequestHelper apiRequestHelper;
    private static final String BASE_URL = "http://localhost:8080/api/bots/";

    public Bot getBot(Long botId) {
        return apiRequestHelper.get(
                BASE_URL + botId,
                Bot.class
        );
    }

    public WorkingHours addWorkingHours(Long botId, WorkingHours workingHour) {
        return apiRequestHelper.post(
                BASE_URL + botId + "/working_hour",
                workingHour,
                WorkingHours.class
        );
    }

    public Job addJob(Long botId, Job job) {
        return  apiRequestHelper.post(
                BASE_URL + botId + "/job",
                job,
                Job.class
        );
    }

    public Job[] getJobs(Long botId) {
        return apiRequestHelper.get(
                BASE_URL + botId + "/jobs",
                Job[].class
        );
    }
}
