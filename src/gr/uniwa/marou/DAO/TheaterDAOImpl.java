package gr.uniwa.marou.DAO;

import gr.uniwa.marou.model.SeatType;
import gr.uniwa.marou.model.Theater;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Map;


/**
 The TheaterDAOImpl class implements the TheaterDAO interface.
 It provides functionality for retrieving the price and availability of seats, updating the availability of seats,
 and booking and canceling seats for the Theater class.
 */
public class TheaterDAOImpl implements  TheaterDAO{

    private final Theater theater;

    public TheaterDAOImpl(Theater theater){
        this.theater = theater;
    }

    /**
     Retrieves the price of the specified seat type.
     @param seatType the type of the seat
     @return the price of the specified seat type
     */
    @Override
    public BigDecimal getPrice(SeatType seatType) {
        return theater.getPrices().get(seatType);
    }

    /**
     Retrieves the availability of all seat types.
     @return a map that maps each seat type to its availability
     */
    @Override
    public Map<SeatType, Integer> getAvailability() {
        return theater.getAvailability();
    }

    /**
     * Calculates the total price for the given number of seats of the specified type.
     *
     * @param seatType the type of seat to calculate the price for
     * @param numberOfSeats the number of seats to calculate the price for
     * @return the total price for the given number of seats of the specified type
     * @throws RemoteException if there is a problem accessing the remote server
     * @throws IllegalArgumentException if the given seat type is not valid
     */
    @Override
    public BigDecimal calculatePrice(SeatType seatType, int numberOfSeats) throws RemoteException {
        BigDecimal pricePerSeat = theater.getPrices().get(seatType);
        if (pricePerSeat == null) {
            throw new RemoteException("Invalid seat type: " + seatType);
        }
        return pricePerSeat.multiply(BigDecimal.valueOf(numberOfSeats));
    }

    /**
     Updates the availability of the specified seat type with the specified number of seats.
     @param seatType the type of the seat
     @param seats the number of seats to update the availability with
     @throws IllegalArgumentException if the number of seats is not positive, or if the seat type is invalid, or if there
     are not enough seats available for the specified seat type
     */
    @Override
    public synchronized void updateAvailability(SeatType seatType, int seats, boolean cancel) {
        if (seats <= 0) {
            throw new IllegalArgumentException("seatCount must be positive");
        }
        Map<SeatType, Integer> availability = getAvailability();
        if (!availability.containsKey(seatType)) {
            throw new IllegalArgumentException("Invalid seatType: " + seatType);
        }
        int currentAvailability = availability.get(seatType);
        int newAvailability = cancel ? currentAvailability + seats : currentAvailability - seats;
        if (newAvailability < 0) {
            throw new IllegalArgumentException("Not enough seats available for seatType: " + seatType);
        }
        availability.put(seatType, newAvailability);
    }


    /**
     Books the specified number of seats of the specified seat type for the specified guest name.
     @param seatType the type of the seat
     @param seats the number of seats to book
     @param guestName the name of the guest booking the seats
     @return true if the seats were successfully booked, false otherwise
     */
    @Override
    public synchronized boolean book(SeatType seatType, int seats, String guestName) {
        try {
            int availableSeats = theater.getAvailability().get(seatType);
            if (availableSeats >= seats) {
                updateAvailability(seatType, seats,false);
                return true;
            }
            return false;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     Cancels the specified number of seats of the specified seat type for the specified guest name.
     @param seatType the type of the seat
     @param numberOfSeats the number of seats to cancel
     @param guestName the name of the guest canceling the seats
     @return true if the seats were successfully canceled, false otherwise
     */
    @Override
    public synchronized boolean cancel(SeatType seatType, int numberOfSeats, String guestName) {
        try {
            updateAvailability(seatType, numberOfSeats, true);
            return true;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
