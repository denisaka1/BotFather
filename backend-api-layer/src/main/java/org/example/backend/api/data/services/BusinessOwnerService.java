package org.example.backend.api.data.services;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.repositories.BusinessOwnerRepository;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BusinessOwner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BusinessOwnerService {

    private final BusinessOwnerRepository businessOwnerRepository;

    public BusinessOwner saveBusinessOwner(BusinessOwner businessOwner) {
        return businessOwnerRepository.save(businessOwner);
    }

    public Optional<Bot> saveBot(Long userTelegramId, Bot bot) {
        return getOwner(userTelegramId).map(owner -> {
            owner.addBot(bot);
            BusinessOwner savedOwner = businessOwnerRepository.save(owner);
            return savedOwner.getBots().get(savedOwner.getBots().size() - 1);
        });
    }

    public Optional<List<Bot>> findAllBots(Long userTelegramId) {
        return getOwner(userTelegramId).map(BusinessOwner::getBots);
    }

    public boolean existsByUserTelegramId(Long userTelegramId) {
        return businessOwnerRepository.existsByUserTelegramId(userTelegramId);
    }

    public BusinessOwner updateBusinessOwner(Long id, BusinessOwner businessOwner) {
        return businessOwnerRepository.findById(id).map(owner -> {
            Optional.ofNullable(businessOwner.getUserTelegramId()).ifPresent(owner::setUserTelegramId);
            Optional.ofNullable(businessOwner.getFirstName()).ifPresent(owner::setFirstName);
            Optional.ofNullable(businessOwner.getLastName()).ifPresent(owner::setLastName);
            Optional.ofNullable(businessOwner.getEmail()).ifPresent(owner::setEmail);
            Optional.ofNullable(businessOwner.getPhoneNumber()).ifPresent(owner::setPhoneNumber);
            Optional.ofNullable(businessOwner.getAddress()).ifPresent(owner::setAddress);
            if (owner.getRegistrationState() != businessOwner.getRegistrationState()) {
                owner.setRegistrationState(businessOwner.getRegistrationState());
            }
            return businessOwnerRepository.save(owner);
        }).orElse(businessOwner);
    }

    public Optional<BusinessOwner> getOwner(Long userTelegramId) {
        return businessOwnerRepository.findByUserTelegramId(userTelegramId);
    }

    public Optional<Bot> getEditableBot(Long userTelegramId) {
        boolean allFieldsPopulated;
        Optional<List<Bot>> findAllBots = findAllBots(userTelegramId);
        if (findAllBots.isEmpty()) {
            return Optional.empty();
        }

        for (Bot bot : findAllBots.get()) {
            allFieldsPopulated = bot.getUsername() != null &&
                    bot.getName() != null &&
                    bot.getWelcomeMessage() != null &&
                    bot.getToken() != null &&
                    !bot.getJobs().isEmpty() &&
                    !bot.getWorkingHours().isEmpty();
            if (allFieldsPopulated && !bot.getCreationState().isCompleted())
                return Optional.of(bot);
        }

        return Optional.empty();
    }

    public Optional<Bot> deleteBot(Long userTelegramId, Long botId) {
        return businessOwnerRepository.findBotByOwnerId(userTelegramId, botId).map(bot -> {
            BusinessOwner owner = bot.getOwner();
            owner.removeBot(bot);
            businessOwnerRepository.save(owner);
            return bot;
        });
    }

    public Optional<List<Bot>> getDisplayableBots(Long userTelegramId) {
        return businessOwnerRepository.findDisplayableBots(userTelegramId).map(bots -> {
            Optional<Bot> bot = getEditableBot(userTelegramId);
            bot.ifPresent(bots::add);
            return bots;
        });
    }
}
