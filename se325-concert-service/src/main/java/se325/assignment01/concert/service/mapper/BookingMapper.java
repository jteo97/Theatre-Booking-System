package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.BookingDTO;
import se325.assignment01.concert.service.domain.Booking;

import java.util.stream.Collectors;

/**
 * Mapper class which maps a Booking object which the database uses to a BookingDTO object which is sent over the wire
 */
public class BookingMapper {

    /**
     * Converts a Booking object to a BookingDTO object
     * @param booking object
     * @return BookingDTO object
     */
    public static BookingDTO toDto(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO(booking.getConcertId(), booking.getDate(),
                booking.getListOfSeats().stream().map(seat -> SeatMapper.toDto(seat)).collect(Collectors.toList()));
        return bookingDTO;
    }
}
