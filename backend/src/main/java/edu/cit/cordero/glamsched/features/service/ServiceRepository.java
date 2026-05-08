package edu.cit.cordero.glamsched.features.service;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByArtistId(Long artistId);
}
