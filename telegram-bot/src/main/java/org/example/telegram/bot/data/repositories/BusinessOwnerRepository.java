package org.example.telegram.bot.data.repositories;

import org.example.telegram.bot.data.entities.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessOwnerRepository extends JpaRepository<BusinessOwner, Long> {
    boolean existsByUserTelegramId(Long userTelegramId);

    Optional<BusinessOwner> findByUserTelegramId(Long userTelegramId);
}
