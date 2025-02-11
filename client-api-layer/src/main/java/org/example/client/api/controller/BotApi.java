package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BotApi {
    private final ApiRequestHelper apiRequestHelper;

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
