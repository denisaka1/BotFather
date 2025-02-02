package org.example.botfather.data.services;

import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.entities.BusinessOwner;
import org.example.botfather.data.repositories.BusinessOwnerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BusinessOwnerService {

    private final BusinessOwnerRepository businessOwnerRepository;

    public List<BusinessOwner> findAll() {
        return businessOwnerRepository.findAll();
    }

    public BusinessOwner saveBusinessOwner(BusinessOwner businessOwner) {
        return businessOwnerRepository.save(businessOwner);
    }

    public Bot saveBot(Long id, Bot bot) {
        BusinessOwner owner = businessOwnerRepository.findById(id).orElseThrow();
        owner.addBot(bot);
        businessOwnerRepository.save(owner);

        return bot;
    }

    public List<Bot> findAllBots(Long id) {
        BusinessOwner owner = businessOwnerRepository.findById(id).orElseThrow();
        return owner.getBots();
    }
}
