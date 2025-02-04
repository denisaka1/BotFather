package org.example.botfather.utils;

import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.entities.Job;
import org.example.botfather.data.entities.WorkingHours;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageExtractor {
    public static String[] extractBotInfoFromForwardedMsg(String input) {
        Matcher usernameMatcher = Pattern.compile("t\\.me/(\\w+)").matcher(input);
        Matcher tokenMatcher = Pattern.compile("Use this token to access the HTTP API:\\s*(\\S+)").matcher(input);
        String username = null;
        String token = null;
        if (usernameMatcher.find()) {
            username = usernameMatcher.group(1);
        }
        if (tokenMatcher.find()) {
            token = tokenMatcher.group(1);
        }
        return new String[]{username, token};
    }

    public static List<WorkingHours> extractWorkingHours(String input, Bot bot) {
        List<WorkingHours> workingHoursList = new ArrayList<>();

        String[] lines = input.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(": ", 2);
            if (parts.length != 2) continue; // Skip invalid lines

            String day = parts[0].trim();
            String timeRange = parts[1].trim();

            // Handle "None" case
            if (timeRange.equalsIgnoreCase("None")) {
                workingHoursList.add(new WorkingHours(null, day, "None", bot));
                continue;
            }

            // Validate time ranges
            Matcher matcher = Pattern.compile("(\\d{2}:\\d{2} - \\d{2}:\\d{2})").matcher(timeRange);
            StringBuilder validRanges = new StringBuilder();
            while (matcher.find()) {
                if (!validRanges.isEmpty()) validRanges.append(", ");
                validRanges.append(matcher.group());
            }

            if (!validRanges.isEmpty()) {
                workingHoursList.add(new WorkingHours(null, day, validRanges.toString(), bot));
            }
        }
        return workingHoursList;
    }

    public static List<Job> extractJobs(String input, Bot bot) {
        List<Job> jobs = new ArrayList<>();
        String[] lines = input.split("\\n");

        Pattern pattern = Pattern.compile("^(.+?):\\s*(\\d{2}:\\d{2}(?:,\\s*\\d{2}:\\d{2})*)$");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());

            if (matcher.find()) {
                String type = matcher.group(1);
                String[] durations = matcher.group(2).split(",\\s*");

                for (String duration : durations) {
                    Job job = new Job();
                    job.setType(type);
                    job.setDuration(parseDurationToHours(duration));
                    job.setOwner(bot);
                    jobs.add(job);
                }
            }
        }
        return jobs;
    }

    private static double parseDurationToHours(String duration) {
        String[] parts = duration.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours + (minutes / 60.0);
    }
}
