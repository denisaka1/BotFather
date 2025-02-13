package org.example.backend.api.data.services;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.BotRepository;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BotService {

    private final BotRepository botRepository;

    public List<Bot> getBots() {
        return botRepository.findAll();
    }

    public Bot saveBot(Bot bot) {
        return botRepository.save(bot);
    }

    public Job saveJob(Long id, Job job) {
        Bot bot = botRepository.findById(id).orElseThrow();
        bot.addJob(job);
        botRepository.save(bot);
        return job;
    }

    public WorkingHours saveWorkingHour(Long id, WorkingHours workingHours) {
        Bot bot = botRepository.findById(id).orElseThrow();
        bot.addWorkingHour(workingHours);
        botRepository.save(bot);
        return workingHours;
    }

    public List<Job> fetchJobs(Long id) {
        Bot bot = botRepository.findById(id).orElseThrow();
        return bot.getJobs();
    }

    public Bot getBot(Long id) {
        return botRepository.findById(id).orElseThrow();
    }

    public Bot updateBot(Long id, Bot bot) {
        Bot oldBot = botRepository.findById(id).orElseThrow();
        if (bot.getName() != null) {
            oldBot.setName(bot.getName());
        }
        if (bot.getUsername() != null) {
            oldBot.setUsername(bot.getUsername());
        }
        if (bot.getToken() != null) {
            oldBot.setToken(bot.getToken());
        }
        if (bot.getWelcomeMessage() != null) {
            oldBot.setWelcomeMessage(bot.getWelcomeMessage());
        }
        if (bot.getCreationState() != oldBot.getCreationState()) {
            oldBot.setCreationState(bot.getCreationState());
        }
        return botRepository.save(oldBot);
    }
}
