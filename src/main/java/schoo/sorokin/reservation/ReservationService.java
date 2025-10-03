package schoo.sorokin.reservation;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservation = reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reservation not found id ={" + id + "}"));
        return toDomainReservation(reservation);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allReservations = reservationRepository.findAll();
        return allReservations.stream().map(this::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("reservationStatus should be null");
        }

        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("reservationEndDate should be after reservationStartDate");
        }

        var entityToSave = new ReservationEntity(null, reservationToCreate.userId(), reservationToCreate.roomId(), reservationToCreate.startDate(), reservationToCreate.endDate(), ReservationStatus.PENDING);

        var newReservation = reservationRepository.save(entityToSave);
        return toDomainReservation(newReservation);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        if (!reservationRepository.existsById(id)) {
            throw new EntityNotFoundException("Reservation not found: " + id);
        }

        var reservationEntity = reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("cannot modify reservation with status " + reservationEntity.getStatus());
        }

        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("reservationEndDate should be after reservationStartDate");
        }

        var reservationToSave = new ReservationEntity(reservationEntity.getId(), reservationToUpdate.userId(), reservationToUpdate.roomId(), reservationToUpdate.startDate(), reservationToUpdate.endDate(), ReservationStatus.PENDING);
        var updatedReservation = reservationRepository.save(reservationToSave);
        return toDomainReservation(updatedReservation);
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new EntityNotFoundException("Reservation not found: " + id);
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
        }
        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel the reservation. Reservation was already cancelled");
        }

        reservationRepository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }


    public Reservation approveReservation(Long id) {
        var reservationEntity = reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("cannot approve reservation should be PENDING");
        }
        if (isReservationConflict(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        )) {
            throw new IllegalArgumentException("cannot approve reservation conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservationEntity);
        return toDomainReservation(reservationEntity);
    }

    private boolean isReservationConflict(Long roomId, LocalDate startDate, LocalDate endDate) {
        List<Long> conflictingIds = reservationRepository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );
        if (conflictingIds.isEmpty()) {
            return false;
        }
        log.info("Conflict with Room id {}, startDate {}, endDate {}", roomId, startDate, endDate);
        return true;
    }


    private Reservation toDomainReservation(ReservationEntity reservationEntity) {
        return new Reservation(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus());
    }
}
