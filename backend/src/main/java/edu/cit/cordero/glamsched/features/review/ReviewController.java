package edu.cit.cordero.glamsched.features.review;

import edu.cit.cordero.glamsched.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<?> getReviews(@PathVariable Long artistId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(reviewRepository.findByArtistIdOrderByCreatedAtDesc(artistId)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("REVIEW_ERROR", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review) {
        try {
            if (review.getArtistId() == null || review.getClientId() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", "Artist and client IDs are required"));
            }
            if (reviewRepository.existsByArtistIdAndClientId(review.getArtistId(), review.getClientId())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("DUPLICATE_REVIEW", "You have already reviewed this artist"));
            }
            review.setId(null);
            review.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return ResponseEntity.ok(ApiResponse.success(reviewRepository.save(review)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("REVIEW_ERROR", e.getMessage()));
        }
    }
}
