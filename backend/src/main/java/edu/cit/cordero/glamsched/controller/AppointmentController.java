package edu.cit.cordero.glamsched.controller;

import edu.cit.cordero.glamsched.dto.ApiResponse;
import edu.cit.cordero.glamsched.dto.AppointmentCreateRequest;
import edu.cit.cordero.glamsched.dto.AppointmentDTO;
import edu.cit.cordero.glamsched.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<AppointmentDTO>> bookAppointment(
            @RequestBody AppointmentCreateRequest request,
            @RequestParam Long clientId) {
        try {
            AppointmentDTO appointment = appointmentService.bookAppointment(request, clientId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, appointment, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("BAD_REQUEST", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getClientAppointments(@PathVariable Long clientId) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getClientAppointments(clientId);
            return ResponseEntity.ok(new ApiResponse<>(true, appointments, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("ERROR", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getArtistAppointments(@PathVariable Long artistId) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getArtistAppointments(artistId);
            return ResponseEntity.ok(new ApiResponse<>(true, appointments, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("ERROR", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> getAppointmentById(@PathVariable Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);
            return ResponseEntity.ok(new ApiResponse<>(true, appointment, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("NOT_FOUND", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestParam String status) {
        try {
            AppointmentDTO appointment = appointmentService.updateAppointmentStatus(appointmentId, status);
            return ResponseEntity.ok(new ApiResponse<>(true, appointment, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("BAD_REQUEST", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long appointmentId) {
        try {
            appointmentService.cancelAppointment(appointmentId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, null, java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, new ApiResponse.ApiError("NOT_FOUND", e.getMessage()), java.time.LocalDateTime.now().toString()));
        }
    }
}
