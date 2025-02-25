package org.example.backend.api.controllers;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.services.BotService;
import org.example.data.layer.entities.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/bots")
public class BotController {

    private final BotService botService;

    @GetMapping("{id}")
    public ResponseEntity<Bot> getBot(@PathVariable Long id) {
        return ResponseEntity.ok(botService.getBot(id));
    }

    @GetMapping("{id}/owner")
    public ResponseEntity<BusinessOwner> getBotOwner(@PathVariable Long id) {
        return ResponseEntity.ok(botService.getBotOwner(id));
    }

    @GetMapping
    public ResponseEntity<List<Bot>> getAllBots() {
        return ResponseEntity.ok(botService.getBots());
    }

    @PostMapping
    public ResponseEntity<Bot> createBot(@RequestBody Bot bot) {
        return ResponseEntity.ok(botService.saveBot(bot));
    }

    @PutMapping("{id}")
    public ResponseEntity<Bot> updateBot(@PathVariable Long id, @RequestBody Bot bot) {
        return ResponseEntity.ok(botService.updateBot(id, bot));
    }

    @PostMapping("{id}/job")
    public ResponseEntity<Job> createJob(@PathVariable Long id, @RequestBody Job job) {
        return ResponseEntity.ok(botService.saveJob(id, job));
    }

    @GetMapping("{id}/jobs")
    public ResponseEntity<List<Job>> getJobs(@PathVariable Long id) {
        return ResponseEntity.ok(botService.fetchJobs(id));
    }

    @PostMapping("{id}/working_hour")
    public ResponseEntity<WorkingHours> createWorkingHour(@PathVariable Long id, @RequestBody WorkingHours workingHours) {
        return ResponseEntity.ok(botService.saveWorkingHour(id, workingHours));
    }

    @GetMapping("{id}/appointments_by_date")
    public ResponseEntity<List<Appointment>> findAppointmentByDate(@PathVariable Long id, @RequestParam String date) {
        return botService.findAppointmentsByDate(id, date);
    }
}
