package org.example.botfather.data.services;
import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Client;
import org.example.botfather.data.repositories.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public ResponseEntity<Client> findByTelegramId(String telegramId) {
        return clientRepository.findByUserTelegramId(telegramId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }
}
