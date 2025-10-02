package schoo.sorokin.reservation;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ErrorResponseDto(
        String message,
        String description,
        LocalDateTime localDateTime
){

}
