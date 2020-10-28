package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.ConcertSummaryDTO;
import se325.assignment01.concert.service.domain.Concert;

import java.util.ArrayList;

/**
 * Mapper class which maps a Concert object which the database uses to a ConcertDTO object which is sent over the wire
 */
public class ConcertMapper {

    /**
     * Converts a Concert object to a ConcertDTO object
     * @param concert object
     * @return ConcertDTO object
     */
    public static ConcertDTO toDto(Concert concert) {
        ConcertDTO concertDTO = new ConcertDTO(concert.getId(), concert.getTitle(),
                concert.getImageName(), concert.getBlurb());
        concertDTO.setDates(new ArrayList<>(concert.getDates()));
        concertDTO.setPerformers(PerformerMapper.setToDto(concert.getPerformers()));
        return concertDTO;
    }

    /**
     * Converts a Concert object to a ConcertSummary object
     * @param concert object
     * @return ConcertSummaryDTO object
     */
    public static ConcertSummaryDTO toSummaryDto(Concert concert) {
        ConcertSummaryDTO concertSummaryDTO = new ConcertSummaryDTO(
                concert.getId(), concert.getTitle(), concert.getImageName()
        );
        return concertSummaryDTO;
    }
}
