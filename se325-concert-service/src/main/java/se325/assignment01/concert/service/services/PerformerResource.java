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
 * This class deals with all requests pertaining to performers e.g. getting performers with id
 */
@Path("/concert-service")
public class PerformerResource {

    /**
     * This HTTP method retrieves the performer matching the id in the database and returns a response along with the DTO object
     * @param id of the performer requested
     * @return response object
     */
    @GET
    @Path("/performers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPerformerWithId(@PathParam("id") long id) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        Performer performer;
        try {
            entityManager.getTransaction().begin();
            performer = entityManager.find(Performer.class, id); //Find the performer in the database matching the id

            //Return a NOT_FOUND error if there is not such performer with that id
            if (performer == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
        return Response.ok(PerformerMapper.toDto(performer)).build(); //Return an OK response along with the performerDTO object
    }

    /**
     * This HTTP method retrieves all the performers in the database and returns the list in a response
     * @return response object
     */
    @GET
    @Path("/performers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPerformers() {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        List<PerformerDTO> performerDTOS = new ArrayList<>();

        try {
            entityManager.getTransaction().begin();
            //Query the database for all performers and store it in a list
            List<Performer> listOfPerformers = entityManager.createQuery("Select p from Performer p", Performer.class).getResultList();
            //Convert all the performers in the list into performerDTO objects so that it can be sent over the wire
            for (Performer performer: listOfPerformers) {
                performerDTOS.add(PerformerMapper.toDto(performer));
            }

        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        //Encapsulate the list in an appropriate object
        GenericEntity<List<PerformerDTO>> response = new GenericEntity<>(performerDTOS){};
        return Response.ok(response).build();
    }
}
