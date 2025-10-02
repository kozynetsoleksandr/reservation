package schoo.sorokin.reservation;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("{id}")
    public Reservation getReservationById(
            @PathVariable("id") Long id
    ) {
        log.info("getReservationById are called, ID: " + id);
        return reservationService.getReservationById(id);
    }

    @GetMapping("")
    public List<Reservation> getAllReservations() {
        log.info("getAllReservations are called");
        return reservationService.findAllReservations();
    }

    @PostMapping("")
    public Reservation createReservation(
            @RequestBody Reservation reservationToCreate
    ) {
        log.info("createReservation are called");
        return reservationService.createReservation(reservationToCreate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody Reservation reservationToUpdate
    ) {
        log.info("updateReservation are called id={}, reservationToUpdate={}", id, reservationToUpdate);
        var updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation: id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok()
                .build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) {
        log.info("approveReservation are called id={}", id);
        var reservation = reservationService.approveReservation(id);

        return ResponseEntity.ok(reservation);
    }
}
