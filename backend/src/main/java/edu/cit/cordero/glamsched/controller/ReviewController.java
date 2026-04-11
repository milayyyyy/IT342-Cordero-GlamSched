package edu.cit.cordero.glamsched.controller;

import edu.cit.cordero.glamsched.dto.ApiResponse;
import edu.cit.cordero.glamsched.dto.ReviewCreateRequest;
import edu.cit.cordero.glamsched.dto.ReviewDTO;
import edu.cit.cordero.glamsched.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @RequestBody ReviewCreateRequest request,
            @RequestParam Long reviewerId) {
        try {
            ReviewDTO review = reviewService.createReview(request, reviewerId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, review, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("BAD_REQUEST", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getArtistReviews(@PathVariable Long artistId) {
        try {
            List<ReviewDTO> reviews = reviewService.getArtistReviews(artistId);
            return ResponseEntity.ok(new ApiResponse<>(true, reviews, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("ERROR", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getAppointmentReview(@PathVariable Long appointmentId) {
        try {
            List<ReviewDTO> reviews = reviewService.getAppointmentReview(appointmentId);
            return ResponseEntity.ok(new ApiResponse<>(true, reviews, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("ERROR", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReviewById(@PathVariable Long reviewId) {
        try {
            ReviewDTO review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(new ApiResponse<>(true, review, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("NOT_FOUND", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/artist/{artistId}/rating")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getArtistAverageRating(@PathVariable Long artistId) {
        try {
            double averageRating = reviewService.getArtistAverageRating(artistId);
            Map<String, Double> response = new HashMap<>();
            response.put("averageRating", averageRating);
            return ResponseEntity.ok(new ApiResponse<>(true, response, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("ERROR", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }
}
