package gr.uniwa.marou.Service;

import gr.uniwa.marou.model.Client;
import gr.uniwa.marou.model.SeatType;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;


/**
 The TheaterRMIService interface defines the methods that can be invoked remotely by a client.
 These methods allow a client to book or cancel seats, list the available seats in the theater,
 retrieve the list of guests who have booked seats, get the availability of seats, add clients to the waiting list,
 and calculate the price of seats.
 The methods may throw a RemoteException if a communication error occurs during the remote method invocation.
 */
public interface TheaterRMIService extends Remote {

    boolean book(SeatType seatType, int seats, String guestName) throws RemoteException;

    boolean cancel(SeatType seatType, int numberOfSeats, String guestName) throws RemoteException;

    StringBuilder list() throws RemoteException;

    String guests() throws RemoteException;

    Map<SeatType, Integer> getAvailability()  throws RemoteException ;

    Map<SeatType, List<Client>> getWaitingList(SeatType seatType) throws RemoteException;

    void addToWaitingList(SeatType seatType, String guestName, int numberOfSeats) throws RemoteException;

    BigDecimal calculatePrice(SeatType seatType, int numberOfSeats) throws RemoteException;

    String notifyGuestsInWaitingList(int numberOfSeats, SeatType seatType) throws RemoteException;
}
