package edu.cit.cordero.glamsched.features.service;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceReactionRepository extends JpaRepository<ServiceReactionEntity, Long> {
    long countByServiceId(Long serviceId);

    boolean existsByServiceIdAndClientId(Long serviceId, Long clientId);

    void deleteByServiceIdAndClientId(Long serviceId, Long clientId);
    void deleteByServiceId(Long serviceId);
    void deleteByClientId(Long clientId);
}
