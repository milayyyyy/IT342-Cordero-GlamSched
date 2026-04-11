package edu.cit.cordero.glamsched.controller;

import edu.cit.cordero.glamsched.dto.ApiResponse;
import edu.cit.cordero.glamsched.dto.ServiceDTO;
import edu.cit.cordero.glamsched.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {
        try {
            List<ServiceDTO> services = serviceService.getAllServices();
            return ResponseEntity.ok(new ApiResponse<>(true, services, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("ERROR", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getServicesByArtist(@PathVariable Long artistId) {
        try {
            List<ServiceDTO> services = serviceService.getServicesByArtist(artistId);
            return ResponseEntity.ok(new ApiResponse<>(true, services, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("ERROR", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceDTO>> getServiceById(@PathVariable Long serviceId) {
        try {
            ServiceDTO service = serviceService.getServiceById(serviceId);
            return ResponseEntity.ok(new ApiResponse<>(true, service, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("NOT_FOUND", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@RequestBody ServiceDTO serviceDTO, @RequestParam Long artistId) {
        try {
            ServiceDTO createdService = serviceService.createService(serviceDTO, artistId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, createdService, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("BAD_REQUEST", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }
}
