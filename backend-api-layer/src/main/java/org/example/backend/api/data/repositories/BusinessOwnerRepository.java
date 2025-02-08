package org.example.backend.api.data.repositories;

import org.example.data.layer.entities.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessOwnerRepository extends JpaRepository<BusinessOwner, Long> {
    boolean existsByUserTelegramId(Long userTelegramId);

    Optional<BusinessOwner> findByUserTelegramId(Long userTelegramId);
}
