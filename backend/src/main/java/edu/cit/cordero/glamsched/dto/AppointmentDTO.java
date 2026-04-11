package edu.cit.cordero.glamsched.dto;

import java.time.LocalDateTime;

public class AppointmentDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long artistId;
    private String artistName;
    private Long serviceId;
    private String serviceName;
    private LocalDateTime appointmentDate;
    private String status;
    private String notes;

    public AppointmentDTO() {}

    public AppointmentDTO(Long id, Long clientId, String clientName, Long artistId, String artistName, Long serviceId, String serviceName, LocalDateTime appointmentDate, String status, String notes) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.artistId = artistId;
        this.artistName = artistName;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
