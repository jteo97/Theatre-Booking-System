package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Seat;

/**
 * Mapper class which maps a Seat object which the database uses to a SeatDTO object which is sent over the wire
 */
public class SeatMapper {

    /**
     * Converts a Seat object to a SeatDTO object
     * @param seat object
     * @return SeatDTO object
     */
    public static SeatDTO toDto(Seat seat) {
        SeatDTO seatDTO = new SeatDTO(seat.getLabel(), seat.getPrice());
        return seatDTO;
    }
}
