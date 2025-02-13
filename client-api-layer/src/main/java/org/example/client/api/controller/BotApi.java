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

    public Bot getBot(Long botId) {
        return apiRequestHelper.get(
                "http://localhost:8080/api/bots/" + botId,
                Bot.class
        );
    }

    public Bot updateBot(Long botId, Bot bot) {
        return apiRequestHelper.put(
                "http://localhost:8080/api/bots/" + botId,
                bot,
                Bot.class
        );
    }

    public WorkingHours addWorkingHours(Long botId, WorkingHours workingHour) {
        return apiRequestHelper.post(
                "http://localhost:8080/api/bots/" + botId + "/working_hour",
                workingHour,
                WorkingHours.class
        );
    }

    public Job addJob(Long botId, Job job) {
        return  apiRequestHelper.post(
                "http://localhost:8080/api/bots/" + botId + "/job",
                job,
                Job.class
        );
    }
}
