package edu.cit.cordero.glamsched.service;

import edu.cit.cordero.glamsched.dto.ServiceDTO;
import edu.cit.cordero.glamsched.model.User;
import edu.cit.cordero.glamsched.repository.ServiceRepository;
import edu.cit.cordero.glamsched.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceDTO createService(ServiceDTO serviceDTO, Long artistId) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        edu.cit.cordero.glamsched.model.Service service = new edu.cit.cordero.glamsched.model.Service();
        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());
        service.setPrice(serviceDTO.getPrice());
        service.setDuration(serviceDTO.getDuration());
        service.setArtist(artist);

        // Convert photos list to JSON string for storage
        if (serviceDTO.getPhotos() != null && !serviceDTO.getPhotos().isEmpty()) {
            try {
                String photosJson = objectMapper.writeValueAsString(serviceDTO.getPhotos());
                service.setPhotos(photosJson);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize photos", e);
            }
        }

        edu.cit.cordero.glamsched.model.Service savedService = serviceRepository.save(service);
        
        // Create DTO with eager-loaded data to avoid lazy loading issues
        ServiceDTO result = new ServiceDTO();
        result.setId(savedService.getId());
        result.setName(savedService.getName());
        result.setDescription(savedService.getDescription());
        result.setPrice(savedService.getPrice());
        result.setDuration(savedService.getDuration());
        result.setArtistId(artist.getId());
        result.setArtistName(artist.getName());
        
        // Deserialize photos
        if (savedService.getPhotos() != null && !savedService.getPhotos().isEmpty()) {
            try {
                List<String> photos = objectMapper.readValue(savedService.getPhotos(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                result.setPhotos(photos);
            } catch (Exception e) {
                System.err.println("Failed to deserialize photos: " + e.getMessage());
            }
        }
        
        return result;
    }

    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ServiceDTO> getServicesByArtist(Long artistId) {
        return serviceRepository.findByArtistId(artistId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ServiceDTO getServiceById(Long serviceId) {
        edu.cit.cordero.glamsched.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        return mapToDTO(service);
    }

    private ServiceDTO mapToDTO(edu.cit.cordero.glamsched.model.Service service) {
        List<String> photos = null;
        
        // Deserialize photos JSON string back to list
        if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
            try {
                photos = objectMapper.readValue(service.getPhotos(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (Exception e) {
                // Log error but don't fail - return service without photos
                System.err.println("Failed to deserialize service photos: " + e.getMessage());
            }
        }
        
        // Initialize artist to avoid lazy loading issues
        Long artistId = null;
        String artistName = null;
        try {
            if (service.getArtist() != null) {
                artistId = service.getArtist().getId();
                artistName = service.getArtist().getName();
            }
        } catch (Exception e) {
            System.err.println("Failed to load artist data: " + e.getMessage());
            artistId = 0L;
            artistName = "Unknown Artist";
        }
        
        return new ServiceDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDuration(),
                artistId,
                artistName,
                photos
        );
    }
}
