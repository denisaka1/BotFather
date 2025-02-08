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
}
