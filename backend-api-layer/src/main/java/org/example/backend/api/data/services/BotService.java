package org.example.backend.api.data.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.BotRepository;
import org.example.data.layer.entities.*;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<List<Appointment>> findAppointmentsByDate(Long id, String date) {
        Bot bot = botRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bot not found with id: " + id));
        LocalDateTime targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        List<Appointment> appointmentsByDate = bot.getAppointments().stream()
                .filter(appointment -> appointment.getAppointmentDate().toLocalDate().equals(targetDate.toLocalDate()))
                .toList();
        return ResponseEntity.ok(appointmentsByDate);
    }
}
