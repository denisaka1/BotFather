package org.example.botfather.data.services;
import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.entities.Job;
import org.example.botfather.data.entities.WorkingHours;
import org.example.botfather.data.repositories.BotRepository;
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
        workingHours.setBot(bot);
        // Ensure the timeRanges list is initialized
        if (workingHours.getTimeRanges() == null) {
            workingHours.setTimeRanges(List.of());
        }
        bot.addWorkingHour(workingHours);
        botRepository.save(bot);
        return workingHours;
    }
}
