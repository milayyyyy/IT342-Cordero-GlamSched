package edu.cit.cordero.glamsched.features.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByArtistId(Long artistId);
}
