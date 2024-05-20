package roomescape;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Controller
public class ReservationController {

    private List<Reservation> reservations = new ArrayList<>();
    private AtomicLong index = new AtomicLong(1);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    // 예약 목록 조회
    @GetMapping("/reservations")
    public ResponseEntity<List<Reservation>> read() {
        String sql = "SELECT * FROM reservation";
        reservations = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Reservation(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("date"),
                        rs.getString("time")
                ));
        return ResponseEntity.ok().body(reservations);
    }

    // id에 따른 예약 조회
    @GetMapping("/reservations/{id}")
    public ResponseEntity<Reservation> readEach(@PathVariable Long id) {
        Reservation reservation = reservations.stream()
                .filter(it -> Objects.equals(it.getId(), id))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        return ResponseEntity.ok().body(reservation);
    }

    // 예약 생성
    @PostMapping("/reservations")
    public ResponseEntity<Reservation> create(@RequestBody Reservation reservation) {
        Reservation newReservation = Reservation.toEntity(reservation, index.getAndIncrement());
        reservations.add(newReservation);
        return ResponseEntity.created(URI.create("/reservations/" + newReservation.getId())).body(newReservation);
    }

    // 예약 삭제
    @DeleteMapping("reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Reservation reservation = reservations.stream()
                .filter(it -> Objects.equals(it.getId(), id))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        reservations.remove(reservation);

        return ResponseEntity.noContent().build();
    }
}
