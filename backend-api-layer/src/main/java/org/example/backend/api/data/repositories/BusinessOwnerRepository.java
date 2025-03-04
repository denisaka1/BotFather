package org.example.backend.api.data.repositories;

import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BusinessOwnerRepository extends JpaRepository<BusinessOwner, Long> {
    boolean existsByUserTelegramId(Long userTelegramId);

    Optional<BusinessOwner> findByUserTelegramId(Long userTelegramId);

    @Query("SELECT b FROM Bot b WHERE b.id = :botId AND b.owner.userTelegramId = :userTelegramId")
    Optional<Bot> findBotByOwnerId(@Param("userTelegramId") Long userTelegramId, @Param("botId") Long botId);

    @Query("SELECT b FROM Bot b WHERE b.creationState = 'COMPLETED' AND b.owner.userTelegramId = :userTelegramId")
    Optional<List<Bot>> findDisplayableBots(Long userTelegramId);
}
