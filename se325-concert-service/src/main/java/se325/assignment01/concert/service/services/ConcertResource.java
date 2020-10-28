package se325.assignment01.concert.service.services;

import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.service.domain.*;
import se325.assignment01.concert.service.mapper.*;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class deals with all requests pertaining to concerts e.g. getting concert with id
 */
@Path("/concert-service")
public class ConcertResource {

    /**
     * This HTTP method retrieves the concert matching the id in the database and returns a response along with the DTO object
     * @param id of the concert requested
     * @return response object
     */
    @GET
    @Path("/concerts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConcertWithId(@PathParam("id") long id) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        Concert concert;
        try {
            entityManager.getTransaction().begin();
            concert = entityManager.find(Concert.class, id); //Find the concert in the database matching the id

            //Return a NOT_FOUND error if there is not such performer with that id
            if (concert == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(ConcertMapper.toDto(concert)).build(); //Return an OK response along with the concertDTO object
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    /**
     * This HTTP method retrieves all the concerts in the database and returns the list in a response
     * @return response object
     */
    @GET
    @Path("/concerts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConcerts() {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        List<ConcertDTO> concertDTOS = new ArrayList<>();

        try {
            entityManager.getTransaction().begin();
            //Query the database for all concerts and store it in a list
            List<Concert> listOfConcerts = entityManager.createQuery("Select c from Concert c", Concert.class).getResultList();
            //Convert all the concerts in the list into concertDTO objects so that it can be sent over the wire
            for (Concert concert: listOfConcerts) {
                concertDTOS.add(ConcertMapper.toDto(concert));
            }

        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        //Encapsulate the list in an appropriate object
        GenericEntity<List<ConcertDTO>> response = new GenericEntity<>(concertDTOS){};
        return Response.ok(response).build();
    }

    /**
     * This HTTP method retrieves all the concerts in the database and returns the list as ConcertSummaryDTO objects in a response
     * @return
     */
    @GET
    @Path("/concerts/summaries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConcertSummaries() {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        List<ConcertSummaryDTO> concertSummaryDto = new ArrayList<>();

        try {
            entityManager.getTransaction().begin();
            //Query the database for all concerts and store it in a list
            List<Concert> listOfConcerts = entityManager.createQuery("Select c from Concert c", Concert.class).getResultList();
            //Convert all the concerts in the list into concertSummaryDTO objects so that it can be sent over the wire
            for (Concert concert: listOfConcerts) {
                concertSummaryDto.add(ConcertMapper.toSummaryDto(concert));
            }

        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        //Encapsulate the list in an appropriate object
        GenericEntity<List<ConcertSummaryDTO>> response = new GenericEntity<>(concertSummaryDto){};
        return Response.ok(response).build();
    }

}

