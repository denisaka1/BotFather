package org.example.backend.api.data.services;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.BotRepository;
import org.example.data.layer.entities.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        getBot(id).ifPresent(bot -> {
            bot.addJob(job);
            botRepository.save(bot);
        });
        return job;
    }

    public WorkingHours saveWorkingHour(Long id, WorkingHours workingHours) {
        return getBot(id).map(bot -> {
            bot.addWorkingHour(workingHours);
            Bot savedBot = botRepository.save(bot);
            return savedBot.getWorkingHours().get(savedBot.getWorkingHours().size() - 1);
        }).orElse(workingHours);
    }

    public Optional<List<Job>> fetchJobs(Long id) {
        return getBot(id).map(Bot::getJobs);
    }

    public Optional<Bot> getBot(Long id) {
        return botRepository.findById(id);
    }

    public Bot updateBot(Long id, Bot incomingBot) {
        Bot botToSave = getBot(id).map(bot -> {
            bot.setName(incomingBot.getName());
            bot.setUsername(incomingBot.getUsername());
            bot.setToken(incomingBot.getToken());
            bot.setWelcomeMessage(incomingBot.getWelcomeMessage());
            bot.setCreationState(incomingBot.getCreationState());

            bot.getJobs().clear();
            bot.addJobs(incomingBot.getJobs());

            bot.getWorkingHours().clear();
            bot.addWorkingHours(incomingBot.getWorkingHours());
            return bot;
        }).orElse(incomingBot);

        return botRepository.save(botToSave);
    }

    public Optional<BusinessOwner> getBotOwner(Long id) {
        return getBot(id).map(Bot::getOwner);
    }

    public Optional<List<Appointment>> findAppointmentsByDate(Long id, String date) {
        return getBot(id).map(bot -> {
            LocalDateTime targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
            return bot.getAppointments().stream()
                    .filter(appointment -> appointment.getAppointmentDate().toLocalDate().equals(targetDate.toLocalDate()))
                    .toList();
        });
    }
}
