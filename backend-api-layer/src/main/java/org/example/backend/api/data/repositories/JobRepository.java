package org.example.backend.api.data.repositories;

import org.example.data.layer.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    @Query("SELECT j from Job j WHERE j.id = :id and j.owner.id = :ownerId")
    Optional<Job> findByIdAndBotId(Long id, Long ownerId);
}
