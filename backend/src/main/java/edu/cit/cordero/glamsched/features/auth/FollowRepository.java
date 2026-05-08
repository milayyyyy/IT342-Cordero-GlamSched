package edu.cit.cordero.glamsched.features.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByClientIdAndArtistId(Long clientId, Long artistId);
    void deleteByClientIdAndArtistId(Long clientId, Long artistId);
    long countByArtistId(Long artistId);
    List<Follow> findByClientId(Long clientId);
    long countByClientId(Long clientId);
}
