package org.example.data.layer.helpers;

import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class BotHelper {
    private BotHelper() {
    }

    public static String info(Bot bot) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Name: ").append(bot.getName()).append("\n");
        stringBuilder.append("Username: ").append(bot.getUsername()).append("\n");
        stringBuilder.append("Welcome message:").append("\n").append(bot.getWelcomeMessage()).append("\n\n");

        stringBuilder.append("Working hours:").append("\n");
        for (WorkingHours workingHour : bot.getWorkingHours()) {
            stringBuilder.append(WorkingHoursHelper.info(workingHour));
        }
        stringBuilder.append("\n");

        stringBuilder.append("Jobs:").append("\n");
        List<Job> sortedJobs = bot.getJobs().stream()
                .sorted(Comparator.comparing(Job::getType))
                .toList();

        if (!sortedJobs.isEmpty()) {
            Job previousJob = sortedJobs.get(0);
            stringBuilder.append(JobHelper.info(previousJob));
            for (Job job : sortedJobs.subList(1, sortedJobs.size())) {
                if (Objects.equals(previousJob.getType(), job.getType())) {
                    stringBuilder.append(", ").append(job.getDuration()).append("h");
                } else {
                    stringBuilder.append("\n").append(JobHelper.info(job));
                }
                previousJob = job;
            }
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
