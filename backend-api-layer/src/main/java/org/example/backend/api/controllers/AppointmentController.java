package org.example.backend.api.controllers;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.services.AppointmentService;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PutMapping("{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointment));
    }

    @GetMapping("{id}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointment(id).orElse(null));
    }

    @GetMapping("/{id}/client")
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getClient(id).orElse(null));
    }

}
