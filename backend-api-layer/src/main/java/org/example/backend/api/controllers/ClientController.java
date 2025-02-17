package org.example.backend.api.controllers;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.services.ClientService;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.example.data.layer.entities.ClientScheduleState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/client")
public class ClientController {
    private final ClientService clientService;

    @GetMapping("{telegramId}")
    public ResponseEntity<Client> findByTelegramId(@PathVariable String telegramId) {
        return clientService.findByTelegramId(telegramId);
    }

    @GetMapping("{telegramId}/appointments")
    public ResponseEntity<List<Appointment>> findAppointments(@PathVariable String telegramId, @RequestParam Long botId) {
        return clientService.findAppointments(telegramId, botId);
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.saveClient(client));
    }

    @PostMapping("{id}/appointment")
    public ResponseEntity<Appointment> createAppointment(
            @RequestBody Appointment appointment,
            @RequestParam Long botId,
            @RequestParam Long jobId,
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.createAppointment(id, appointment, botId, jobId));
    }

    @PutMapping("{userTelegramId}/schedule_state")
    public ResponseEntity<Client> updateScheduleState(@PathVariable String userTelegramId, @RequestBody ClientScheduleState clientScheduleState) {
        return ResponseEntity.ok(clientService.updateScheduleState(userTelegramId, clientScheduleState));
    }

    @PutMapping("{userTelegramId}")
    public ResponseEntity<Client> updateClient(@PathVariable String userTelegramId, @RequestBody Client client) {
        return ResponseEntity.ok(clientService.updateClient(userTelegramId, client));
    }
}
