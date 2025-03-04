package org.example.backend.api.data.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.api.data.repositories.ClientRepository;
import org.example.backend.api.data.repositories.JobRepository;
import org.example.data.layer.entities.Appointment;
import org.example.data.layer.entities.Client;
import org.example.data.layer.entities.Job;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final JobRepository jobRepository;

    public Optional<Client> findByTelegramId(String telegramId) {
        return clientRepository.findByTelegramId(telegramId);
    }

    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Transactional
    public Optional<Appointment> createAppointment(Long clientId, Appointment appointment, Long botId, Long jobId) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            log.warn("Failed to create appointment: Client with ID {} not found", clientId);
            return Optional.empty();
        }

        Optional<Job> jobOpt = jobRepository.findByIdAndBotId(jobId, botId);
        if (jobOpt.isEmpty()) {
            log.warn("Failed to create appointment: Job with ID {} not found for Bot ID {}", jobId, botId);
            return Optional.empty();
        }

        Client client = clientOpt.get();
        Job job = jobOpt.get();
        appointment.setJob(job);
        client.addAppointment(appointment, job.getOwner());

        return Optional.of(clientRepository.save(client).getLastAppointment());
    }

    public Client updateClient(String userTelegramId, Client incomingClient) {
        return findByTelegramId(userTelegramId).map(client -> {
            Optional.ofNullable(incomingClient.getName()).ifPresent(client::setName);
            Optional.ofNullable(incomingClient.getEmail()).ifPresent(client::setEmail);
            Optional.ofNullable(incomingClient.getPhoneNumber()).ifPresent(client::setPhoneNumber);
            return clientRepository.save(client);
        }).orElse(incomingClient);
    }

    public ResponseEntity<List<Appointment>> findAppointments(String telegramId, Long botId) {
        Client client = clientRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with telegramId: " + telegramId));

        List<Appointment> appointmentsByBot = client.getAppointments().stream()
                .filter(appointment -> appointment.getBot().getId().equals(botId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentsByBot);
    }

    public Appointment deleteAppointment(String id, String appointmentId) {
        Client client = clientRepository.findByTelegramId(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with telegramId: " + id));
        Appointment appointment = client.getAppointments().stream()
                .filter(a -> a.getId().toString().equals(appointmentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + appointmentId));
        client.removeAppointment(appointment);
        clientRepository.save(client);
        return appointment;
    }

    public ResponseEntity<List<Appointment>> findAppointmentsByDate(String telegramId, String botId, String date) {
        Client client = clientRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with telegramId: " + telegramId));
        LocalDateTime targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        List<Appointment> appointmentsByDate = client.getAppointments().stream()
                .filter(appointment -> appointment.getBot().getId().toString().equals(botId) &&
                        appointment.getAppointmentDate().toLocalDate().equals(targetDate.toLocalDate()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointmentsByDate);
    }

    public ResponseEntity<List<Client>> findAllClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }
}
