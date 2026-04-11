package edu.cit.cordero.glamsched.service;

import edu.cit.cordero.glamsched.dto.AppointmentCreateRequest;
import edu.cit.cordero.glamsched.dto.AppointmentDTO;
import edu.cit.cordero.glamsched.model.Appointment;
import edu.cit.cordero.glamsched.model.User;
import edu.cit.cordero.glamsched.repository.AppointmentRepository;
import edu.cit.cordero.glamsched.repository.ServiceRepository;
import edu.cit.cordero.glamsched.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public AppointmentDTO bookAppointment(AppointmentCreateRequest request, Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        User artist = userRepository.findById(request.getArtistId())
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        edu.cit.cordero.glamsched.model.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setArtist(artist);
        appointment.setService(service);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToDTO(savedAppointment);
    }

    public List<AppointmentDTO> getClientAppointments(Long clientId) {
        return appointmentRepository.findByClientIdOrderByAppointmentDateDesc(clientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getArtistAppointments(Long artistId) {
        return appointmentRepository.findByArtistIdOrderByAppointmentDateDesc(artistId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AppointmentDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return mapToDTO(appointment);
    }

    public AppointmentDTO updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        try {
            appointment.setStatus(Appointment.AppointmentStatus.valueOf(status));
            Appointment updatedAppointment = appointmentRepository.save(appointment);
            return mapToDTO(updatedAppointment);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid appointment status");
        }
    }

    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private AppointmentDTO mapToDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getClient().getName(),
                appointment.getArtist().getId(),
                appointment.getArtist().getName(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getAppointmentDate(),
                appointment.getStatus().toString(),
                appointment.getNotes()
        );
    }
}
