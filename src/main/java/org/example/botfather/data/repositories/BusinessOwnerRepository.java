package org.example.botfather.data.repositories;

import org.example.botfather.data.entities.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessOwnerRepository extends JpaRepository<BusinessOwner, Long> {
}
