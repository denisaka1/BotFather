package org.example.botfather.data.repositories;

import org.example.botfather.data.entities.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotRepository extends JpaRepository<Bot, Long> {
}
