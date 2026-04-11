package edu.cit.cordero.glamsched.repository;

import edu.cit.cordero.glamsched.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientId(Long clientId);
    List<Appointment> findByArtistId(Long artistId);
    List<Appointment> findByClientIdOrderByAppointmentDateDesc(Long clientId);
    List<Appointment> findByArtistIdOrderByAppointmentDateDesc(Long artistId);
}
