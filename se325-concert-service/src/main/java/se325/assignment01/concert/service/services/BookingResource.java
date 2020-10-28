package se325.assignment01.concert.service.services;

import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.common.types.BookingStatus;
import se325.assignment01.concert.service.domain.*;
import se325.assignment01.concert.service.jaxrs.LocalDateTimeParam;
import se325.assignment01.concert.service.mapper.*;
import se325.assignment01.concert.service.util.Config;
import se325.assignment01.concert.service.util.Subscription;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/concert-service")
public class BookingResource {

    //A thread pool to handle the async response
    private ExecutorService threads = Executors.newCachedThreadPool();
    //A map to hold the subscribers to each concert (Key = concertIf, Value = all subscribers)
    private static final Map<Long, List<Subscription>> subscribersToConcert = new ConcurrentHashMap<>();

    /**
     * This HTTP method makes a booking
     * @param bookingRequestDTO
     * @param cookie
     * @return
     */
    @POST
    @Path("/bookings")
    public Response makeBooking(BookingRequestDTO bookingRequestDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        Booking booking;
        User user;
        Concert concert;

        if (cookie == null) { //If cookie is null means client is unauthorised to perform this action
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int numSeatsAvailable, totalNumSeats;
        try {
            entityManager.getTransaction().begin();
            user = getUser(cookie, entityManager); //Call the helper method to see if user is authenticated
            if (user == null) { //If user is null means client is unauthorised to perform this action
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Query the database to find the concert
            concert = entityManager.find(Concert.class, bookingRequestDTO.getConcertId());
            if (concert == null) { //If concert is null means there is no such concert in the database
                return Response.status(Response.Status.BAD_REQUEST).build(); //Return BAD_REQUEST response
            } else if (!concert.getDates().contains(bookingRequestDTO.getDate())) {
                //If the booking is for a not existent date for a concert
                return Response.status(Response.Status.BAD_REQUEST).build(); //Return BAD_REQUEST response
            }

            booking = book(user, bookingRequestDTO); //Call the helper method to place a booking on a concert
            if (booking == null) { //If booking is null means action of booking is not doable
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            //Query to obtain all available seats for a given date
            TypedQuery<Seat> availableQuery = entityManager.createQuery(
                    "select s from Seat s where s.date = :date and s.isBooked = :isBooked", Seat.class)
                    .setParameter("date", bookingRequestDTO.getDate()).setParameter("isBooked", false)
                    .setLockMode(LockModeType.OPTIMISTIC); //Set lock mode to prevent concurrency issues
            numSeatsAvailable = availableQuery.getResultList().size();

            //Query to obtain all seats for a given date
            TypedQuery<Seat> allQuery = entityManager.createQuery(
                    "select s from Seat s where s.date = :date", Seat.class)
                    .setParameter("date", bookingRequestDTO.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC); //Set lock mode to prevent concurrency issues
            totalNumSeats = allQuery.getResultList().size();

        } catch (OptimisticLockException e) { //If this exception is thrown send back a bad request response
            return Response.status(Response.Status.BAD_REQUEST).build();
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        //A call to helper method to send notification to subscribers of a concert when a successful booking has been made
        sendNotificationsToSubs(bookingRequestDTO.getConcertId(), bookingRequestDTO.getDate(),
                numSeatsAvailable,totalNumSeats);
        //Send back the generated URI to the client in a 201 response
        return Response.created(URI.create("/concert-service/bookings/" + booking.getId())).build();
    }

    /**
     * This HTTP method retrieves the booking matching the id and returns it in a response
     * @param id of the booking
     * @param cookie
     * @return response object
     */
    @GET
    @Path("/bookings/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingsWithId(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        User user;
        BookingDTO bookingDTO;

        if (cookie == null) { //If cookie is null means client is unauthorised to perform this action
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            entityManager.getTransaction().begin();
            user = getUser(cookie, entityManager); //Call the helper method to see if user is authenticated
            if (user == null) { //If user is null means client is unauthorised to perform this action
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Retrieve the booking from the database matching the id
            Booking booking = entityManager.find(Booking.class, id);
            if (booking == null) { //If booking is null means booking is not in the database
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //If the booking user id is not the same as the user id making the request then they are forbidden to make this request
            if (booking.getUser().getId() != user.getId()) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            bookingDTO = BookingMapper.toDto(booking); //Make the booking to a bookingDTO to be sent over the wire
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        return Response.ok(bookingDTO).build();
    }

    /**
     * This HTTP method retrieves all the bookings made by this user using their cookie information as identification
     * @param cookie
     * @return
     */
    @GET
    @Path("/bookings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingsOfUser(@CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        User user;
        List<BookingDTO> bookingDTOS = new ArrayList<>();

        if (cookie == null) { //If cookie is null means client is unauthorised to perform this action
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            entityManager.getTransaction().begin();
            user = getUser(cookie, entityManager); //Call the helper method to see if user is authenticated
            if (user == null) { //If user is null means client is unauthorised to perform this action
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Query the database for all the bookings made by the user
            TypedQuery<Booking> typedQuery = entityManager.createQuery("select b from Booking b where b.user = :user", Booking.class)
                    .setParameter("user", user).setLockMode(LockModeType.OPTIMISTIC); //Set lock mode to prevent concurrency issues
            List<Booking> bookingList = typedQuery.getResultList();
            //Convert all the bookings in the list into bookingDTO objects so that it can be sent over the wire
            for (Booking booking: bookingList) {
                bookingDTOS.add(BookingMapper.toDto(booking));
            }
        } catch (OptimisticLockException e) { //If this exception is thrown send back a bad request response
            return Response.status(Response.Status.BAD_REQUEST).build();
        } finally {
            entityManager.close();
        }

        //Encapsulate the list in an appropriate object
        GenericEntity<List<BookingDTO>> response = new GenericEntity<>(bookingDTOS){};
        return Response.ok(response).build();
    }

    /**
     * This HTTP method Retrieves all the seats of a date of a concert
     * @param status
     * @param dateTimeParam
     * @return
     */
    @GET
    @Path("/seats/{dateTime}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkSeats(@QueryParam("status")BookingStatus status, @PathParam("dateTime") LocalDateTimeParam dateTimeParam) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        LocalDateTime dateTime = dateTimeParam.getLocalDateTime();
        List<SeatDTO> seatDTOS = new ArrayList<>();
        try {
            entityManager.getTransaction().begin();
            TypedQuery<Seat> typedQuery; //Query is different based on the status passed in
            if (status == BookingStatus.Any) {
                typedQuery = entityManager.createQuery(
                        "select s from Seat s where s.date = :date", Seat.class)
                        .setParameter("date", dateTime)
                        .setLockMode(LockModeType.OPTIMISTIC); //Set lock mode to prevent concurrency issues
            } else {
                boolean isBooked = (status == BookingStatus.Booked) ? true : false;
                typedQuery = entityManager.createQuery(
                        "select s from Seat s where s.date = :date and s.isBooked = :isBooked", Seat.class)
                        .setParameter("date", dateTime).setParameter("isBooked", isBooked)
                        .setLockMode(LockModeType.OPTIMISTIC); //Set lock mode to prevent concurrency issues
            }

            List<Seat> listOfSeats = typedQuery.getResultList();
            //Convert all the seats in the list into seatDTO objects so that it can be sent over the wire
            for (Seat seat: listOfSeats) {
                seatDTOS.add(SeatMapper.toDto(seat));
            }

        } catch (OptimisticLockException e) { //If this exception is thrown send back a bad request response
            return Response.status(Response.Status.BAD_REQUEST).build();
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        //Encapsulate the list in an appropriate object
        GenericEntity<List<SeatDTO>> response = new GenericEntity<>(seatDTOS){};
        return Response.ok(response).build();
    }

    /**
     * This HTTP method is to subscribe to the notification system the web service provides
     * @param concertInfoSubscriptionDTO
     * @param response
     * @param cookie
     */
    @POST
    @Path("/subscribe/concertInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void subscribe(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO,
                          @Suspended AsyncResponse response, @CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        User user;
        long id;
        Concert concert;

        if (cookie == null) { //If cookie is null means client is unauthorised to perform this action
            //Instead of returning using the handling thread, use another thread specific to handle these types of responses
            threads.submit(() -> response.resume(Response.status(Response.Status.UNAUTHORIZED).build()));
            return;
        }

        try {
            entityManager.getTransaction().begin();
            user = getUser(cookie, entityManager); //Call the helper method to see if user is authenticated
            if (user == null) { //If user is null means client is unauthorised to perform this action
                //Instead of returning using the handling thread, use another thread specific to handle these types of responses
                threads.submit(() -> response.resume(Response.status(Response.Status.UNAUTHORIZED).build()));
                return;
            }

            id = concertInfoSubscriptionDTO.getConcertId();
            concert = entityManager.find(Concert.class, id); //Find the concert in the database which matches the id
            if (concert == null) { //If concert is null means no such concert in the database
                //Instead of returning using the handling thread, use another thread specific to handle these types of responses
                threads.submit(() -> response.resume(Response.status(Response.Status.BAD_REQUEST).build()));
                return;
            }
            //If there are dates in the client is trying to sign up for notifications for that are not valid dates return BAD_REQUEST
            else if (!(concert.getDates().contains(concertInfoSubscriptionDTO.getDate()))) {
                //Instead of returning using the handling thread, use another thread specific to handle these types of responses
                threads.submit(() -> response.resume(Response.status(Response.Status.BAD_REQUEST).build()));
                return;
            }

            //If code makes it this far then we add the valid subscription into the subscriber map
            List<Subscription> subs = subscribersToConcert.getOrDefault(concert.getId(), new ArrayList<>());
            Subscription subscription = new Subscription(concertInfoSubscriptionDTO, response);
            subs.add(subscription);
            subscribersToConcert.put(concert.getId(), subs); //Replace the subscriber list in the map

        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    /**
     * Helper method to notify the subscribers of the particular concert with certain conditions are met
     * @param concertId
     * @param dateTime of the concert
     * @param numSeatsAvailable in the concert
     * @param totalNumSeats in the conert
     */
    public void sendNotificationsToSubs(long concertId, LocalDateTime dateTime, int numSeatsAvailable, int totalNumSeats) {
        //Obtain the subscriber list of the concert
        List<Subscription> subscriptionList = subscribersToConcert.get(concertId);

        if (subscriptionList == null) { //If the list is null meaning that no-one subscribed, don't do anything and return
            return;
        }

        for (Subscription sub: subscriptionList) { //For each subscriber we perform a series of checks to see if we should notify them
            ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO = sub.getConcertInfoSubscriptionDTO();
            int percentageBooked = sub.getConcertInfoSubscriptionDTO().getPercentageBooked();

            //If the date of the concert the user is subscribed to is the same as the new booking date
            if (dateTime.isEqual(concertInfoSubscriptionDTO.getDate())) {

                //Perform calculation to see if the number of unavailable seats is more the threshold
                //The threshold is a user defined percentage for which if the percentage of unavailable seats exceeds
                //This amount, they want to receive a notification
                boolean aboutToSellOut;
                double percentageOfSeatsBooked = (100 - ((numSeatsAvailable*100/totalNumSeats)));
                aboutToSellOut = (percentageBooked <= percentageOfSeatsBooked) ? true : false;

                if (aboutToSellOut) { //If the threshold for notifying the subscriber is met then notify them
                    AsyncResponse response = sub.getResponse(); //Obtain the async response from the subscribe object
                    ConcertInfoNotificationDTO concertInfoNotificationDTO = new ConcertInfoNotificationDTO(numSeatsAvailable);
                    response.resume(Response.ok(concertInfoNotificationDTO).build()); //Notify the user about the impending sellout
                }
            }
        }
    }

    /**
     * Helper method to book concerts
     * @param user
     * @param bookingRequestDTO
     * @return booking object
     */
    private Booking book(User user, BookingRequestDTO bookingRequestDTO) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        Booking booking;
        List<String> seatLabels = bookingRequestDTO.getSeatLabels();
        LocalDateTime date = bookingRequestDTO.getDate();
        long concertId = bookingRequestDTO.getConcertId();

        try {
            entityManager.getTransaction().begin();

            //Retrieve all the seats that are not booked on the date of the user made booking
            TypedQuery<Seat> typedQuery = entityManager.createQuery(
                    "select s from Seat s where s.label in :seats and s.isBooked = :status and s.date = :date", Seat.class)
                    .setParameter("seats", seatLabels).setParameter("status", false)
                    .setParameter("date", date)
                    .setLockMode(LockModeType.OPTIMISTIC); //Set lock mode to prevent concurrency issues
            List<Seat> listOfSeatsAvailable = typedQuery.getResultList();

            //If there are seats that are booked among the seats the user is booking return null
            if (!(listOfSeatsAvailable.size() == seatLabels.size())) {
                return null; //Return null because the system does not allow for placing bookings where seats are booked
            }
            //At this point all the seats in the user booking can be booked and so set all the seats to booked
            for (Seat seatToBook: listOfSeatsAvailable) {
                seatToBook.setBooked(true);
            }

            booking = new Booking(date, concertId, listOfSeatsAvailable, user);
            entityManager.persist(booking); //Persisting the booking will persist the seats as well
        } catch (OptimisticLockException e) { //If this exception is thrown try to book again
            entityManager.close();
            booking = book(user, bookingRequestDTO);
        }
        finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
        return booking;
    }

    /**
     * Helper method to authenticate user
     * @param cookie of the user
     * @param entityManager
     * @return user object
     */
    private User getUser(Cookie cookie, EntityManager entityManager) {
        //Finds the user with the matching cookie
        TypedQuery<User> userQuery = entityManager.createQuery("select u from User u where u.cookie = :cookie", User.class)
                .setParameter("cookie", cookie.getValue());
        User user = userQuery.getResultList().stream().findFirst().orElse(null);
        return user; //Return the user if retrieved otherwise return null
    }
}

