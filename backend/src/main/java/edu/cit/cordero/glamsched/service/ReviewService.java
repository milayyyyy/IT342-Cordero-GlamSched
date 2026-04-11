package edu.cit.cordero.glamsched.service;

import edu.cit.cordero.glamsched.dto.ReviewCreateRequest;
import edu.cit.cordero.glamsched.dto.ReviewDTO;
import edu.cit.cordero.glamsched.model.Appointment;
import edu.cit.cordero.glamsched.model.Review;
import edu.cit.cordero.glamsched.model.User;
import edu.cit.cordero.glamsched.repository.AppointmentRepository;
import edu.cit.cordero.glamsched.repository.ReviewRepository;
import edu.cit.cordero.glamsched.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    public ReviewDTO createReview(ReviewCreateRequest request, Long reviewerId) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User artist = userRepository.findById(request.getArtistId())
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Review review = new Review();
        review.setAppointment(appointment);
        review.setReviewer(reviewer);
        review.setArtist(artist);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        return mapToDTO(savedReview);
    }

    public List<ReviewDTO> getArtistReviews(Long artistId) {
        return reviewRepository.findByArtistIdOrderByCreatedAtDesc(artistId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewDTO> getAppointmentReview(Long appointmentId) {
        return reviewRepository.findByAppointmentId(appointmentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return mapToDTO(review);
    }

    public double getArtistAverageRating(Long artistId) {
        List<Review> reviews = reviewRepository.findByArtistId(artistId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private ReviewDTO mapToDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getAppointment().getId(),
                review.getReviewer().getId(),
                review.getReviewer().getName(),
                review.getArtist().getId(),
                review.getArtist().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
