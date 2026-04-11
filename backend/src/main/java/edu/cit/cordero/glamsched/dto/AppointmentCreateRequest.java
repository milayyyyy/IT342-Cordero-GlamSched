package edu.cit.cordero.glamsched.dto;

import java.time.LocalDateTime;

public class AppointmentCreateRequest {
    private Long artistId;
    private Long serviceId;
    private LocalDateTime appointmentDate;
    private String notes;

    public AppointmentCreateRequest() {}

    public AppointmentCreateRequest(Long artistId, Long serviceId, LocalDateTime appointmentDate, String notes) {
        this.artistId = artistId;
        this.serviceId = serviceId;
        this.appointmentDate = appointmentDate;
        this.notes = notes;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
