package org.example.backend.api.data.services;
import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.ClientRepository;
import org.example.data.layer.entities.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public ResponseEntity<Client> findByTelegramId(String telegramId) {
        return clientRepository.findByTelegramId(telegramId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }
}
