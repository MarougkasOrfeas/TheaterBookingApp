package gr.uniwa.marou.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 The Client class represents a booking made by a guest. It contains information about the guest name,
 the number of seats booked, and the seat type.
 This class provides methods to check for equality and calculate hash codes based on the guest name, number of seats
 booked, and the seat type.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Client implements Serializable {

    private String guestName;
    private int numberOfSeats;
    private SeatType seatType;

    /**
     Checks whether this Client object is equal to the given object.
     @param o the object to be compared for equality
     @return true if the objects are equal, false otherwise
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return numberOfSeats == client.numberOfSeats &&
                Objects.equals(guestName, client.guestName) &&
                seatType == client.seatType;
    }

    /**
     Returns a hash code for this Client object.
     @return a hash code value for this object
     */
    public int hashCode() {
        return Objects.hash(guestName, numberOfSeats, seatType);
    }

    /**
     Returns a notification message for a cancellation of a number of seats with a given seat type.
     @param seatType the seat type of the cancelled seats.
     @param numberOfSeats the number of cancelled seats.
     @return the notification message for the cancellation.
     */
    public String notifyCancellation(SeatType seatType, int numberOfSeats) {
        return  "A cancellation has been made for " + numberOfSeats + " seat(s) with code: " + seatType + ".";
    }
}
