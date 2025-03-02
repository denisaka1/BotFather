package org.example.backend.api.controllers;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.services.ClientService;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/client")
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<Client>> findAllClients() {
        return clientService.findAllClients();
    }

    @GetMapping("{telegramId}")
    public ResponseEntity<Client> findByTelegramId(@PathVariable String telegramId) {
        return clientService.findByTelegramId(telegramId);
    }

    @GetMapping("{telegramId}/appointments")
    public ResponseEntity<List<Appointment>> findAppointments(@PathVariable String telegramId, @RequestParam Long botId) {
        return clientService.findAppointments(telegramId, botId);
    }

    @GetMapping("{telegramId}/appointments_by_date")
    public ResponseEntity<List<Appointment>> findAppointmentByDate(@PathVariable String telegramId, @RequestParam String botId, @RequestParam String date) {
        return clientService.findAppointmentsByDate(telegramId, botId, date);
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

    @PutMapping("{id}/appointment/{appointmentId}")
    public ResponseEntity<Appointment> deleteAppointment(@PathVariable String appointmentId, @PathVariable String id) {
        return ResponseEntity.ok(clientService.deleteAppointment(id, appointmentId));
    }

    @PutMapping("{userTelegramId}")
    public ResponseEntity<Client> updateClient(@PathVariable String userTelegramId, @RequestBody Client client) {
        return ResponseEntity.ok(clientService.updateClient(userTelegramId, client));
    }
}
