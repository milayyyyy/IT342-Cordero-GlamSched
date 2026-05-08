package edu.cit.cordero.glamsched.features.review;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long artistId;
    private Long clientId;
    private String clientName;
    private int rating; // 1–5
    private String comment;
    private String createdAt;
}
