package edu.cit.cordero.glamsched.dto;

public class ReviewCreateRequest {
    private Long appointmentId;
    private Long artistId;
    private Integer rating; // 1-5
    private String comment;

    public ReviewCreateRequest() {}

    public ReviewCreateRequest(Long appointmentId, Long artistId, Integer rating, String comment) {
        this.appointmentId = appointmentId;
        this.artistId = artistId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
