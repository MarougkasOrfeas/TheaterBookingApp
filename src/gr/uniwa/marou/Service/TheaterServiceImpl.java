package gr.uniwa.marou.Service;

import gr.uniwa.marou.DAO.TheaterDAO;
import gr.uniwa.marou.model.Client;
import gr.uniwa.marou.model.SeatType;
import gr.uniwa.marou.model.Theater;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Implementation of the TheaterService and TheaterRMIService interfaces.
 */
public class TheaterServiceImpl extends UnicastRemoteObject  implements TheaterRMIService, TheaterService{

    private final TheaterDAO theaterDAO;
    private final List<Client> clients;
    private final Map<SeatType, List<Client>> waitingList;

    public TheaterServiceImpl(TheaterDAO theaterDAO) throws RemoteException {
        super();
        this.theaterDAO = theaterDAO;
        this.clients = new ArrayList<>();
        this.waitingList = new HashMap<>();
    }

    /**
     * Adds a client to the clients list.
     * @param client the client to add
     */
    public synchronized void addClient(Client client) {
        // Guest name doesn't exist in the list, add a new client
        clients.add(client);
    }

    /**
     * Removes a client from the clients list.
     * @param guestName the name of the guest to remove
     */
    public synchronized void removeClient(String guestName) {
        clients.removeIf(c -> c.getGuestName().equals(guestName));
    }

    /**
     Returns a Map containing the waiting list for a specific seat type, or the entire waiting list if no seat type is specified.
     @param seatType the SeatType to filter the waiting list by, or null to return the entire waiting list
     @return a Map containing the waiting list, where the keys are SeatTypes and the values are Lists of Clients
     @throws RemoteException if a communication-related exception occurs
     */
    @Override
    public synchronized Map<SeatType, List<Client>> getWaitingList(SeatType seatType) throws RemoteException {
        Map<SeatType, List<Client>> result = new HashMap<>();
        if (seatType == null) {
            result.putAll(waitingList);
        } else {
            List<Client> clients = waitingList.get(seatType);
            if (clients != null) {
                result.put(seatType, new ArrayList<>(clients));
            }
        }
        return result;
    }

    /**
     Adds a new Client to the waiting list for a specific seat type.
     @param seatType the SeatType to add the Client to
     @param guestName the name of the guest to add
     @param numberOfSeats the number of seats the guest wants to book
     @throws RemoteException if a communication-related exception occurs
     @throws IllegalArgumentException if guestName is null or empty
     */
    @Override
    public void addToWaitingList(SeatType seatType, String guestName, int numberOfSeats) throws RemoteException {
        synchronized (waitingList) {
            if (guestName == null || guestName.isEmpty()) {
                throw new IllegalArgumentException("Guest name is required");
            }
            List<Client> clients = waitingList.getOrDefault(seatType, new ArrayList<>());
            clients.add(new Client(guestName, numberOfSeats,seatType));
            waitingList.put(seatType, clients);
            System.out.println("Successfully added " + guestName + " to the waiting list for " + numberOfSeats + " " + seatType + " seats.");
        }
    }

    /**
     Calculates the price of a given number of seats of a specific type.
     @param seatType the type of seat to calculate the price for
     @param numberOfSeats the number of seats to calculate the price for
     @return the price of the given number of seats of the specified type
     @throws RemoteException if there is a problem with the remote method call
     */
    @Override
    public synchronized BigDecimal calculatePrice(SeatType seatType, int numberOfSeats) throws RemoteException {
        return theaterDAO.calculatePrice(seatType, numberOfSeats);
    }

    /**
     Notifies clients in the waiting list for a specific seat type and number of seats that a cancellation has occurred.
     @param numberOfSeats the number of seats that were cancelled
     @param seatType the type of seat that was cancelled
     @return a message to be displayed to the clients in the waiting list
     @throws RemoteException if there is an issue with the remote method call
     */
    @Override
    public synchronized String notifyGuestsInWaitingList(int numberOfSeats, SeatType seatType) throws RemoteException {
        Map<SeatType, List<Client>> waitingList = getWaitingList(seatType);
        if (waitingList != null && waitingList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (List<Client> clients : waitingList.values()) {
                for (Client client : clients) {
                    String msg = client.notifyCancellation(client.getSeatType(),client.getNumberOfSeats()); // Call the desired method on the Client object
                    sb.append(msg).append("\n");
                }
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
        }
        return null;
    }

    /**
     * Retrieves the price of a specific seat type.
     * @param seatType the seat type to get the price for
     * @return the price of the seat type
     * @throws RemoteException if there is a problem with the remote invocation
     */
    @Override
    public synchronized BigDecimal getPrice(SeatType seatType) throws RemoteException {
        return theaterDAO.getPrice(seatType);
    }

    /**
     * Retrieves the availability of each seat type.
     * @return a map with the availability of each seat type
     * @throws RemoteException if there is a problem with the remote invocation
     */
    @Override
    public synchronized Map<SeatType, Integer> getAvailability() throws RemoteException {
        return theaterDAO.getAvailability();
    }

    /**
     * Updates the availability of a specific seat type.
     * @param seatType the seat type to update the availability for
     * @param seats    the number of seats to update the availability with
     * @throws RemoteException if there is a problem with the remote invocation
     */
    @Override
    public synchronized void updateAvailability(SeatType seatType, int seats, boolean cancel) throws RemoteException {
        theaterDAO.updateAvailability(seatType,seats, cancel);
    }

    /**
    Books the specified number of seats of the specified seat type for the specified guest name.
    If there are enough seats available, the seats will be booked for the guest and a client object will be added to the client list.
    If there are not enough seats available, the guest will be added to the waiting list and notified accordingly.
    If the booking is successful and there are any waiting guests for the same seat type, the first guest in the waiting list will be booked and notified.
    @param seatType the type of the seat
    @param seats the number of seats to book
    @param guestName the name of the guest booking the seats
    @return true if the seats were successfully booked, false otherwise
    @throws RemoteException if a communication-related exception occurs
    */
    @Override
    public synchronized boolean book(SeatType seatType, int seats, String guestName) throws RemoteException {
        // Try to book from the waiting list if possible
        tryBookingFromWaitingList(seatType);
        int availableSeats = theaterDAO.getAvailability().get(seatType);
        if(availableSeats < seats){
            System.out.println("Sorry, there are only " + availableSeats + " " + seatType + " seats available.");
            return false;
        }
        boolean success = theaterDAO.book(seatType,seats,guestName);
        if (success) {
            Client client = new Client(guestName, seats, seatType);
            addClient(client);
            return true;
        }else{
            // Seat type is full, add client to waiting list
            List<Client> clients = waitingList.getOrDefault(seatType, new ArrayList<>());
            clients.add(new Client(guestName, seats, seatType));
            waitingList.put(seatType, clients);
            System.out.println("Sorry, the requested " + seats + " " + seatType
                    + " seats are currently unavailable. You have been added to the waiting list.");
            return false;
        }
    }

    /**
    Cancels the specified number of seats of the given type that were booked by the guest with the given name.
    Also updates the booking of the guest if they have remaining seats after the cancellation.
    @param seatType the type of the seat to cancel
    @param numberOfSeats the number of seats to cancel
    @param guestName the name of the guest who booked the seats
    @return true if the cancellation was successful, false otherwise (e.g., if the guest does not have enough seats to cancel)
    @throws RemoteException if there is a remote communication error with the server
    */
    @Override
    public synchronized boolean cancel(SeatType seatType, int numberOfSeats, String guestName) throws RemoteException {
        boolean success = theaterDAO.cancel(seatType,numberOfSeats, guestName);
        if (success) {
            // Check if the guest has remaining seats in the booking
            for (Client client : clients) {
                if (client.getGuestName().equals(guestName)) {
                    int allSeats = client.getNumberOfSeats();
                    if (numberOfSeats > allSeats) {
                        return false; // user can't cancel more seats than they have booked
                    }
                    int remainingSeats = allSeats - numberOfSeats;
                    if (remainingSeats > 0) {
                        // update the existing booking for the guest with the remaining seats
                        client.setNumberOfSeats(remainingSeats);
                    } else {
                        removeClient(guestName);
                    }
                    break;
                }
            }
            // Try to book from the waiting list if possible
            tryBookingFromWaitingList(seatType);
            // Notify the waiting list if a cancellation has been made on a specific seat
            notifyGuestsInWaitingList(numberOfSeats, seatType);
        }
        return success;
    }

    /**
     * Attempts to book seats for a waiting client of the given seat type, if any are on the waiting list.
     * If a booking is made, the client is removed from the waiting list and added to the list of current clients.
     * The waiting list for the specified seat type is emptied after successful bookings.
     * @param seatType the type of seat to book for a waiting client
     * @throws RemoteException if there is a problem communicating with the theater server
     */
    private synchronized void tryBookingFromWaitingList(SeatType seatType) throws RemoteException {
        // Check if there are any clients waiting for this seat type
        List<Client> waitingClients;

        synchronized (waitingList) {
            waitingClients = new ArrayList<>(waitingList.getOrDefault(seatType, Collections.emptyList()));
        }

        if (!waitingClients.isEmpty()) {
            List<Client> bookedClients = new ArrayList<>();

            for (Client c : waitingClients) {
                boolean successWaiting;

                synchronized (theaterDAO) {
                    successWaiting = theaterDAO.book(c.getSeatType(), c.getNumberOfSeats(), c.getGuestName());
                }

                if (!successWaiting) {
                    return;
                }

                synchronized (this) {
                    addClient(c);
                    System.out.println("Successfully booked " + c.getNumberOfSeats() + " " + c.getSeatType()
                            + " seat(s) for " + c.getGuestName() + " from the waiting list.");
                }

                bookedClients.add(c);
            }

            synchronized (waitingList) {
                waitingClients.removeAll(bookedClients);
                // Empty the waitingList for the specified seatType
                waitingList.remove(seatType);
            }
        }
    }

    /**
     Returns a StringBuilder object containing a list of available seats and their prices.
     The list is created by calling the getAvailability and getPrice methods of the theaterDAO object.
     The StringBuilder object is formatted with the number of seats, seat type, seat code, and price.
     @return a StringBuilder object containing a list of available seats and their prices.
     @throws RemoteException if there is a remote communication problem.
     */
    @Override
    public synchronized StringBuilder list() throws RemoteException {
        StringBuilder availableSeats = new StringBuilder();
        Map<SeatType, Integer> availability = theaterDAO.getAvailability();
        for (SeatType seatType : SeatType.values()) {
            int seats = availability.get(seatType);
            BigDecimal price = theaterDAO.getPrice(seatType);
            availableSeats
                    .append(seats)
                    .append(" Seats ")
                    .append(seatType)
                    .append(" (Code: ")
                    .append(seatType.name())
                    .append(") - Price: ")
                    .append(price)
                    .append(" €\n");
        }
        return availableSeats;
    }

    /**
     Returns a string containing information about the guests who have booked seats for the show.
     The string includes the number of guests and for each guest, their name, the number of seats they have booked,
     and the type of seats they have booked.
     @return a string containing information about the guests who have booked seats for the show.
     @throws RemoteException if a communication-related exception occurs.
     */
    @Override
    public synchronized String guests() throws RemoteException {
        int people = clients.size();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("There are %d people for the show.\n", people));
        System.out.printf("There are %d people for the show.\n", people);

        for(Client client : clients){
            sb.append(String.format("%s has %d seats in %s.\n",client.getGuestName(), client.getNumberOfSeats(), client.getSeatType()));
            System.out.printf("%s has %d seats in %s.\n", client.getGuestName(), client.getNumberOfSeats(), client.getSeatType());
        }
        return sb.toString();
    }
}
