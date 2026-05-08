package edu.cit.cordero.glamsched.features.payment;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clientId;
    private Long appointmentId;
    private String serviceName;
    private String artistName;
    private Double amount;
    private String status; // PENDING, COMPLETED, FAILED
    private String paymentMethod;
    private LocalDateTime createdAt = LocalDateTime.now();
}
