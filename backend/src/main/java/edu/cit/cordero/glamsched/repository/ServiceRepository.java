package edu.cit.cordero.glamsched.repository;

import edu.cit.cordero.glamsched.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByArtistId(Long artistId);
    List<Service> findAll();
}
