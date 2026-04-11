package edu.cit.cordero.glamsched.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private Long appointmentId;
    private Long reviewerId;
    private String reviewerName;
    private Long artistId;
    private String artistName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewDTO() {}

    public ReviewDTO(Long id, Long appointmentId, Long reviewerId, String reviewerName, Long artistId, String artistName, Integer rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.artistId = artistId;
        this.artistName = artistName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
