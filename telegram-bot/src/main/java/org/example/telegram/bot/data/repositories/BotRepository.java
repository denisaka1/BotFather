package org.example.telegram.bot.data.repositories;

import org.example.telegram.bot.data.entities.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotRepository extends JpaRepository<Bot, Long> {
}
