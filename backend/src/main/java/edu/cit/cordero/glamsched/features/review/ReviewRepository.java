package edu.cit.cordero.glamsched.features.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByArtistIdOrderByCreatedAtDesc(Long artistId);
    boolean existsByArtistIdAndClientId(Long artistId, Long clientId);
    void deleteByArtistId(Long artistId);
    void deleteByClientId(Long clientId);
}
