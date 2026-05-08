package edu.cit.cordero.glamsched.features.service;

import edu.cit.cordero.glamsched.features.auth.User;
import edu.cit.cordero.glamsched.features.auth.UserRepository;
import edu.cit.cordero.glamsched.features.auth.FollowRepository;
import edu.cit.cordero.glamsched.shared.dto.ApiResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceReactionRepository reactionRepository;
    private final FollowRepository followRepository;

    public ServiceController(ServiceRepository serviceRepository, UserRepository userRepository,
            ServiceReactionRepository reactionRepository, FollowRepository followRepository) {
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.reactionRepository = reactionRepository;
        this.followRepository = followRepository;
    }

    private ServiceDTO toDTO(ServiceEntity s) {
        return toDTO(s, null);
    }

    private ServiceDTO toDTO(ServiceEntity s, Long clientId) {
        String artistName = "Unknown Artist";
        String artistProfileImage = null;
        if (s.getArtistId() != null) {
            User artist = userRepository.findById(s.getArtistId()).orElse(null);
            if (artist != null) {
                artistName = artist.getName();
                artistProfileImage = artist.getProfileImage();
            }
        }
        ServiceDTO dto = new ServiceDTO(s, artistName, artistProfileImage);
        dto.setReactionCount(reactionRepository.countByServiceId(s.getId()));
        if (clientId != null) {
            dto.setLikedByMe(reactionRepository.existsByServiceIdAndClientId(s.getId(), clientId));
            dto.setFollowedByMe(s.getArtistId() != null && followRepository.existsByClientIdAndArtistId(clientId, s.getArtistId()));
        }
        return dto;
    }

    @GetMapping
    public ApiResponse<List<ServiceDTO>> getAllServices(@RequestParam(required = false) Long clientId) {
        List<ServiceDTO> dtos = serviceRepository.findAll().stream()
                .map(s -> toDTO(s, clientId))
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @GetMapping("/artist/{artistId}")
    public ApiResponse<List<ServiceDTO>> getServicesByArtist(@PathVariable Long artistId,
            @RequestParam(required = false) Long clientId) {
        List<ServiceDTO> dtos = serviceRepository.findByArtistId(artistId).stream()
                .map(s -> toDTO(s, clientId))
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @PostMapping("/{id}/react")
    @Transactional
    public ApiResponse<Map<String, Object>> toggleReaction(@PathVariable Long id, @RequestParam Long clientId) {
        boolean alreadyLiked = reactionRepository.existsByServiceIdAndClientId(id, clientId);
        if (alreadyLiked) {
            reactionRepository.deleteByServiceIdAndClientId(id, clientId);
        } else {
            reactionRepository.save(new ServiceReactionEntity(id, clientId));
        }
        long count = reactionRepository.countByServiceId(id);
        return ApiResponse.success(Map.of("likedByMe", !alreadyLiked, "reactionCount", count));
    }

    @PatchMapping("/{id}/photos")
    public ApiResponse<ServiceDTO> updateServicePhotos(@PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        ServiceEntity s = serviceRepository.findById(id).orElse(null);
        if (s == null)
            return ApiResponse.error("NOT_FOUND", "Service not found");
        @SuppressWarnings("unchecked")
        java.util.List<String> photos = (java.util.List<String>) body.get("photos");
        if (photos != null) {
            s.setPhotos(photos);
            serviceRepository.save(s);
        }
        return ApiResponse.success(toDTO(s));
    }

    @PatchMapping("/{id}")
    public ApiResponse<ServiceDTO> updateService(@PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        ServiceEntity s = serviceRepository.findById(id).orElse(null);
        if (s == null) return ApiResponse.error("NOT_FOUND", "Service not found");
        if (body.containsKey("name"))        s.setName((String) body.get("name"));
        if (body.containsKey("description")) s.setDescription((String) body.get("description"));
        if (body.containsKey("category"))    s.setCategory((String) body.get("category"));
        if (body.containsKey("price")) {
            Object p = body.get("price");
            if (p instanceof Number) s.setPrice(((Number) p).doubleValue());
        }
        serviceRepository.save(s);
        return ApiResponse.success(toDTO(s));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteService(@PathVariable Long id) {
        ServiceEntity s = serviceRepository.findById(id).orElse(null);
        if (s == null) return ApiResponse.error("NOT_FOUND", "Service not found");
        reactionRepository.deleteByServiceId(s.getId());
        serviceRepository.deleteById(s.getId());
        return ApiResponse.success("Service deleted");
    }

    @PostMapping("/create")
    public ApiResponse<ServiceDTO> createService(@RequestParam Long artistId,
            @RequestBody java.util.Map<String, Object> body) {
        ServiceEntity s = new ServiceEntity();
        s.setArtistId(artistId);
        s.setName((String) body.getOrDefault("name", ""));
        s.setDescription((String) body.getOrDefault("description", ""));
        Object priceObj = body.get("price");
        if (priceObj instanceof Number)
            s.setPrice(((Number) priceObj).doubleValue());
        Object durationObj = body.get("duration");
        if (durationObj != null)
            s.setDuration(String.valueOf(durationObj));
        s.setCategory((String) body.getOrDefault("category", ""));
        @SuppressWarnings("unchecked")
        java.util.List<String> photos = (java.util.List<String>) body.get("photos");
        if (photos != null)
            s.setPhotos(photos);
        ServiceEntity saved = serviceRepository.save(s);
        return ApiResponse.success(toDTO(saved));
    }
}
