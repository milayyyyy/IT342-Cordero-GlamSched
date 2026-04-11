package edu.cit.cordero.glamsched.repository;

import edu.cit.cordero.glamsched.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByArtistId(Long artistId);
    List<Review> findByReviewerId(Long reviewerId);
    List<Review> findByAppointmentId(Long appointmentId);
    List<Review> findByArtistIdOrderByCreatedAtDesc(Long artistId);
}
