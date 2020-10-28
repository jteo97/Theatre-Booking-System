package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Performer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper class which maps a Performer object which the database uses to a PerformerDTO object which is sent over the wire
 */
public class PerformerMapper {

    /**
     * Converts a Performer object to a PerformerDTO object
     * @param performer object
     * @return PerformerDTO object
     */
    public static PerformerDTO toDto(Performer performer) {
        PerformerDTO performerDTO = new PerformerDTO(performer.getId(), performer.getName(),
                performer.getImageName(), performer.getGenre(), performer.getBlurb());
        return performerDTO;
    }

    /**
     * Converts a Set of performers to a list of performers
     * @param setOfPerformers
     * @return list of performers
     */
    public static List<PerformerDTO> setToDto(Set<Performer> setOfPerformers) {
        List<Performer> listOfPerformer = new ArrayList<>(setOfPerformers);
        List<PerformerDTO> performerDTOList = new ArrayList<>();
        performerDTOList = listOfPerformer.stream().map(performer -> PerformerMapper.toDto(performer)).collect(Collectors.toList());
        return performerDTOList;
    }
}
