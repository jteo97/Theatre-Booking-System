package se325.assignment01.concert.service.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The Booking class models that of a booking. It contains fields such as a generated ID, the user who
 * made the booking, list of seats that the booking is on, date of the booking, and the concert id
 */
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Database generated ID to uniquely identify booking

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; //The user who made the booking to keep track

    @OneToMany(cascade = CascadeType.PERSIST)
    @Fetch(FetchMode.SUBSELECT) //Subselect mode to optimise the retrieval of the seats if the booking is retrieved
    private List<Seat> listOfSeats; //List of seats that the user booked

    private LocalDateTime date; //Date of the booking

    private Long concertId; //Concert for which the booking is made on

    public Booking() {

    }

    public Booking(LocalDateTime date, Long concertId, List<Seat> listOfSeats, User user) {
        this.user = user;
        this.date = date;
        this.concertId = concertId;
        this.listOfSeats = listOfSeats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Seat> getListOfSeats() {
        return listOfSeats;
    }

    public void setListOfSeats(List<Seat> listOfSeats) {
        this.listOfSeats = listOfSeats;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }
}
