package org.example.backend.api.data.repositories;

import org.example.data.layer.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT app from Appointment app WHERE app.client.telegramId = :telegramId AND app.status IN ('APPROVED', 'CANCELED') ORDER BY app.id DESC")
    Optional<Appointment> findByOwnerResponseAndClientTelegramId(@Param("telegramId") Long telegramId);
}
