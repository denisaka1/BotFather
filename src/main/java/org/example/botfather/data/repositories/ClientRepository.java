package org.example.botfather.data.repositories;
import org.example.botfather.data.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository  extends JpaRepository<Client, Long> {
    Optional<Client> findByUserTelegramId(String telegramId);
}
