package se325.assignment01.concert.service.services;

import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.service.domain.*;
import se325.assignment01.concert.service.mapper.*;
import se325.assignment01.concert.service.util.Config;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.UUID;

/**
 * This class deals with all requests pertaining to login e.g. logging in
 */
@Path("/concert-service")
public class LoginResource {

    /**
     * This HTTP method checks if user credentials is correct and sends back a auth-cookie if verification succeeds
     * @param userDTO
     * @return response object
     */
    @POST
    @Path("/login")
    public Response login(UserDTO userDTO) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();

        try {
            entityManager.getTransaction().begin();
            //Retrieves the user object in the database with the matching username and password
            TypedQuery<User> typedQuery = entityManager.createQuery(
                    "select u from User u where u.username = :username and u.password = :password", User.class)
                    .setParameter("username", userDTO.getUsername()).setParameter("password", userDTO.getPassword());

            //Gets the user from the database query result
            User user = typedQuery.getResultList().stream().findFirst().orElse(null);

            if (user == null) { //If user is null means that user is not in database meaning not authorised
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else { //Otherwise create a auth-cookie and send it back to client
                NewCookie cookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
                user.setCookie(cookie.getValue());
                //Since User object has a cookie field, we need to update database so that database knows the client is authorised
                entityManager.merge(user);

                return Response.ok().cookie(cookie).build();
            }
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }
}
