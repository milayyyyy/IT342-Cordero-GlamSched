package edu.cit.cordero.glamsched.features.booking;

import edu.cit.cordero.glamsched.features.auth.User;
import edu.cit.cordero.glamsched.features.auth.UserRepository;
import edu.cit.cordero.glamsched.features.service.ServiceEntity;
import edu.cit.cordero.glamsched.features.service.ServiceRepository;
import edu.cit.cordero.glamsched.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                  UserRepository userRepository,
                                  ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public ApiResponse<List<Appointment>> getAppointments(@RequestParam Long clientId) {
        return ApiResponse.success(appointmentRepository.findByClientId(clientId));
    }

    @GetMapping("/artist/{artistId}")
    public ApiResponse<List<Appointment>> getArtistAppointments(@PathVariable Long artistId) {
        return ApiResponse.success(appointmentRepository.findByArtistId(artistId));
    }

    @PostMapping
    public ApiResponse<Appointment> createAppointment(@RequestBody Appointment appointment) {
        // Ensure this request is always treated as a new row insert.
        // Some clients send id=0, which can trigger stale update errors.
        appointment.setId(null);
        appointment.setStatus("PENDING");
        return ApiResponse.success(appointmentRepository.save(appointment));
    }

    @PostMapping("/book")
    public ApiResponse<Appointment> bookAppointment(@RequestParam Long clientId, @RequestBody Map<String, Object> body) {
        Appointment apt = new Appointment();
        apt.setId(null);
        apt.setClientId(clientId);
        apt.setStatus("PENDING");

        if (body.get("artistId") != null) {
            Long artistId = Long.valueOf(body.get("artistId").toString());
            apt.setArtistId(artistId);
            userRepository.findById(artistId).ifPresent(u -> apt.setArtistName(u.getName()));
        }

        if (body.get("serviceId") != null) {
            Long serviceId = Long.valueOf(body.get("serviceId").toString());
            apt.setServiceId(serviceId);
            serviceRepository.findById(serviceId).ifPresent(s -> apt.setServiceName(s.getName()));
        }

        userRepository.findById(clientId).ifPresent(u -> apt.setClientName(u.getName()));

        String dateTime = body.get("appointmentDate") != null ? body.get("appointmentDate").toString() : "";
        if (dateTime.contains("T")) {
            String[] parts = dateTime.split("T");
            apt.setDate(parts[0]);
            apt.setTime(parts.length > 1 ? parts[1] : "");
        } else {
            apt.setDate(dateTime);
        }

        apt.setNotes(body.get("notes") != null ? body.get("notes").toString() : "");

        return ApiResponse.success(appointmentRepository.save(apt));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Appointment> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        apt.setStatus(status);
        return ApiResponse.success(appointmentRepository.save(apt));
    }
}
