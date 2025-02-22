package org.example.backend.api.data.services;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.BotRepository;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BusinessOwner;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Bot updateBot(Long id, Bot incomingBot) {
        Optional<Bot> optionalExistingBot = botRepository.findById(id);

        if (optionalExistingBot.isPresent()) {
            Bot existingBot = optionalExistingBot.get();

            existingBot.setName(incomingBot.getName());
            existingBot.setUsername(incomingBot.getUsername());
            existingBot.setToken(incomingBot.getToken());
            existingBot.setWelcomeMessage(incomingBot.getWelcomeMessage());
            existingBot.setCreationState(incomingBot.getCreationState());

            existingBot.getJobs().clear();
            existingBot.addJobs(incomingBot.getJobs());

            existingBot.getWorkingHours().clear();
            existingBot.addWorkingHours(incomingBot.getWorkingHours());

            return botRepository.save(existingBot);
        } else {
            return botRepository.save(incomingBot);
        }
    }

    public BusinessOwner getBotOwner(Long id) {
        Bot bot = botRepository.findById(id).orElseThrow();
        return bot.getOwner();
    }
}
