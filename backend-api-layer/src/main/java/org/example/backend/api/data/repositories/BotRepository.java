package org.example.backend.api.data.repositories;

import org.example.data.layer.entities.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//@EnableJpaRepositories(basePackageClasses = BotRepository.class)
public interface BotRepository extends JpaRepository<Bot, Long> {
}
