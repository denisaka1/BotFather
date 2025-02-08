package org.example.backend.api.data.services;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.BotRepository;
import org.example.backend.api.data.repositories.BusinessOwnerRepository;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BusinessOwner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BusinessOwnerService {

    private final BusinessOwnerRepository businessOwnerRepository;
    private final BotRepository botRepository;

    public List<BusinessOwner> findAll() {
        return businessOwnerRepository.findAll();
    }

    public BusinessOwner saveBusinessOwner(BusinessOwner businessOwner) {
        return businessOwnerRepository.save(businessOwner);
    }

    public Bot saveBot(Long userTelegramId, Bot bot) {
        BusinessOwner owner = businessOwnerRepository.findByUserTelegramId(userTelegramId).orElseThrow();
        Bot savedBot = botRepository.save(bot);
        owner.addBot(savedBot);
        businessOwnerRepository.save(owner);

        return savedBot;
    }

    public List<Bot> findAllBots(Long userTelegramId) {
        BusinessOwner owner = businessOwnerRepository.findByUserTelegramId(userTelegramId).orElseThrow();
        return owner.getBots();
    }

    public boolean existsByUserTelegramId(Long userTelegramId) {
        return businessOwnerRepository.existsByUserTelegramId(userTelegramId);
    }
}
