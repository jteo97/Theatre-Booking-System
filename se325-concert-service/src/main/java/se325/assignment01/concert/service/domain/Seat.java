package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The Seat class models that of a seat in a concert. It contains fields such as a generated ID, the seat label e.g. "A0",
 * the date of the seat, whether the seat is booked or not, the price of the seat, and a version tracker to see if updated
 */
@Entity
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //Database generated ID to uniquely identify seat

	private String label; //Seat label to identify which seat this is in the concert

	private LocalDateTime date; //The date of the seat

	private boolean isBooked; //Holds information whether the seat is booked or not

	private BigDecimal price; //The price of the seat

	@Version
	private Long version; //A version to see if the seat is updated, for concurrency control

	public Seat() {}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public void setBooked(boolean booked) {
		isBooked = booked;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
