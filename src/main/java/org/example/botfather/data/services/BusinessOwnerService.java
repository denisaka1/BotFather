package org.example.botfather.data.services;

import org.example.botfather.data.entities.BusinessOwner;
import org.example.botfather.data.repositories.BusinessOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessOwnerService {

    private final BusinessOwnerRepository businessOwnerRepository;

    public BusinessOwnerService(BusinessOwnerRepository businessOwnerRepository) {
        this.businessOwnerRepository = businessOwnerRepository;
    }

    public List<BusinessOwner> findAll() {
        return businessOwnerRepository.findAll();
    }

    public BusinessOwner saveBusinessOwner(BusinessOwner businessOwner) {
        return businessOwnerRepository.save(businessOwner);
    }
}
