package org.example.telegram.bot.redis.repository;

import org.example.telegram.bot.redis.entity.BotSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotSessionRepository extends CrudRepository<BotSession, Long> {

}
