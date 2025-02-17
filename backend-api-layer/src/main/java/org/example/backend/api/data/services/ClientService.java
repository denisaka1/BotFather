package org.example.backend.api.data.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.BotRepository;
import org.example.backend.api.data.repositories.ClientRepository;
import org.example.data.layer.entities.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final BotRepository botRepository;

    public ResponseEntity<Client> findByTelegramId(String telegramId) {
        return clientRepository.findByTelegramId(telegramId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Transactional
    public Appointment createAppointment(Long clientId, Appointment appointment, Long botId, Long jobId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new EntityNotFoundException("Bot not found with id: " + botId));
        Optional<Job> job = bot.getJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst();
        job.ifPresentOrElse(
                appointment::setJob,
                () -> System.out.println("Job not found")
        );
        client.addAppointment(appointment, bot);
        return clientRepository.save(client).getLastAppointment();
    }

    public Client updateScheduleState(String telegramId, ClientScheduleState clientScheduleState) {
        Client client = clientRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with telegramId: " + telegramId));
        client.updateScheduleState(clientScheduleState);
        return clientRepository.save(client);
    }

    public Client updateClient(String userTelegramId, Client client) {
        Client oldClient = clientRepository.findByTelegramId(userTelegramId).orElseThrow();
        if (client.getName() != null) {
            oldClient.setName(client.getName());
        }
        if (client.getEmail() != null) {
            oldClient.setEmail(client.getEmail());
        }
        if (client.getPhoneNumber() != null) {
            oldClient.setPhoneNumber(client.getPhoneNumber());
        }
        return clientRepository.save(oldClient);
    }
}
