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
        BusinessOwner owner = getOwner(userTelegramId);
//        Bot savedBot = botRepository.save(bot);
        owner.addBot(bot);
        businessOwnerRepository.save(owner);

        return owner.getBots().get(owner.getBots().size() - 1);
    }

    public List<Bot> findAllBots(Long userTelegramId) {
        BusinessOwner owner = getOwner(userTelegramId);
        return owner.getBots();
    }

    public boolean existsByUserTelegramId(Long userTelegramId) {
        return businessOwnerRepository.existsByUserTelegramId(userTelegramId);
    }

    public BusinessOwner updateBusinessOwner(Long id, BusinessOwner businessOwner) {
        BusinessOwner currentOwner = businessOwnerRepository.findById(id).orElseThrow();
        if (businessOwner.getUserTelegramId() != null) {
            currentOwner.setUserTelegramId(businessOwner.getUserTelegramId());
        }
        if (businessOwner.getFirstName() != null) {
            currentOwner.setFirstName(businessOwner.getFirstName());
        }
        if (businessOwner.getLastName() != null) {
            currentOwner.setLastName(businessOwner.getLastName());
        }
        if (businessOwner.getEmail() != null) {
            currentOwner.setEmail(businessOwner.getEmail());
        }
        if (businessOwner.getPhoneNumber() != null) {
            currentOwner.setPhoneNumber(businessOwner.getPhoneNumber());
        }
        if (businessOwner.getAddress() != null) {
            currentOwner.setAddress(businessOwner.getAddress());
        }
        if (businessOwner.getRegistrationState() != currentOwner.getRegistrationState()) {
            currentOwner.setRegistrationState(businessOwner.getRegistrationState());
        }
        return businessOwnerRepository.save(currentOwner);
    }

    public BusinessOwner getOwner(Long userTelegramId) {
        return businessOwnerRepository.findByUserTelegramId(userTelegramId).orElseThrow();
    }
}
