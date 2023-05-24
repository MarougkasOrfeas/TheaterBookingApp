package gr.uniwa.marou.DAO;

import gr.uniwa.marou.model.SeatType;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Map;


/**
 The TheaterDAO interface provides the methods for accessing and updating the data related to the theater seats
 and the booking information.
 */
public interface TheaterDAO {

    BigDecimal getPrice(SeatType seatType);

    void updateAvailability(SeatType seatType, int seats, boolean cancel);
    boolean book(SeatType seatType, int seats, String guestName);
    boolean cancel(SeatType seatType, int numberOfSeats, String guestName);

    Map<SeatType, Integer> getAvailability() throws RemoteException;

    BigDecimal calculatePrice(SeatType seatType, int numberOfSeats) throws RemoteException;
}
