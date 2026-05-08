package edu.cit.cordero.glamsched.features.booking;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clientId;
    private Long artistId;
    private Long serviceId;
    private String serviceName;
    private String artistName;
    private String clientName;
    private String date;
    private String time;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();
}
