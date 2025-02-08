package org.example.backend.api.data.repositories;

import org.example.data.layer.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository  extends JpaRepository<Client, Long> {
    Optional<Client> findByTelegramId(String telegramId);
}
