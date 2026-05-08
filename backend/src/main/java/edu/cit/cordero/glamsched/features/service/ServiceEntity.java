package edu.cit.cordero.glamsched.features.service;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
@Data
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String duration;
    private Long artistId;

    @Convert(converter = StringListConverter.class)
    @Column(name = "photos", columnDefinition = "TEXT")
    private List<String> photos = new ArrayList<>();
}
