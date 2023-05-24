package gr.uniwa.marou.Service;

import gr.uniwa.marou.model.SeatType;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;


/**
 The TheaterService interface provides the methods for interacting with the theater's data, which include:
 Getting the price of a specific seat type
 Getting the availability of all seat types
 Updating the availability of a specific seat type
 */
public interface TheaterService extends Remote {
    BigDecimal getPrice(SeatType seatType) throws RemoteException;

    Map<SeatType, Integer> getAvailability() throws RemoteException;

    void updateAvailability(SeatType seatType, int seats,boolean cancel) throws RemoteException;
}
