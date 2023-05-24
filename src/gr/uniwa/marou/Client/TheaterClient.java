package gr.uniwa.marou.Client;

import gr.uniwa.marou.Service.TheaterRMIService;
import gr.uniwa.marou.model.ConsoleColors;
import gr.uniwa.marou.model.SeatType;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * A client for the Theater Booking application. This class allows users to connect to a remote TheaterRMIService using RMI
 * and perform various actions, such as listing available seats, booking seats for a guest, retrieving a list of guests who have
 * booked seats, and canceling a booking for a given guest and seat number.
 */
public class TheaterClient extends UnicastRemoteObject {

    private static final int PORT_NUMBER = 9999;
    private static final String USAGE_MESSAGE = """
            \t#MENU#
            ****************************************************************
            1. Display all available seats[args]: list <hostname> <TheaterName>
            2. To Book specific <SeatType> and desired <number> of seats in your <name>[args]: book <hostname> <SeatType> <number> <name>
            3. Display Booked List[args]: guests <hostname>
            4. To Cancel a Booking[args]: cancel <hostname> <SeatType> <number> <name>
            """;
    private static final String ERROR_MESSAGE =ConsoleColors.RED+ "Invalid command format."+ConsoleColors.RESET + "Usage:\n" + USAGE_MESSAGE;
    private static final String THEATER_NAME = "MyTheater";

    protected TheaterClient() throws RemoteException {
    }

    /**
     The entry point of the Theater Booking App.
     This method connects to a remote TheaterRMIService using RMI, and allows the user to perform various actions
     by passing arguments to the command line. The available commands are:
     list: to list all available seats in the theater.
     book: to book one or more seats for a given guest name and phone number.
     guests: to retrieve a list of all guests who have booked seats.
     cancel: to cancel a booking for a given guest name and seat number.
     Usage: java gr.uniwa.marou.Client <command> [arguments]
     @param args an array of Strings representing the command and its arguments.
     */
    public static void main(String[] args) {
        try{
            // Create the RMI URL using the local host address and the port number
            String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":" + PORT_NUMBER + "/TheaterService";
            // Look up the remote object using the RMI URL
            Remote remoteObject = Naming.lookup(url);
            // Cast the remote object to the TheaterRMIService interface
            TheaterRMIService remoteServer = (TheaterRMIService) remoteObject;

            System.out.println("Welcome to the Theater booking app");

            //check if no arguments were provided when running the program
            if (args.length == 0) {
                System.out.println(USAGE_MESSAGE);
                System.exit(0);
            }

            // Use a switch statement to execute the appropriate command based on the user input
            switch (args[0].toLowerCase()) {
                case "list" -> handleListCommand(args, remoteServer);
                case "book" -> handleBookCommand(args, remoteServer);
                case "guests" -> handleGuestsCommand(args, remoteServer);
                case "cancel" -> handleCancelCommand(args, remoteServer);
                default -> System.out.println(USAGE_MESSAGE);
            }
        }catch (Exception e){
            System.err.println("gr.uniwa.marou.Client exception: " + e);
            e.printStackTrace();
        }
    }

    /**
     Handles the "list" command by verifying the input, checking the name of the theater, and calling the remote server's
     "list" method to get the list of all seats in the theater and their availability status.
     @param args the arguments passed to the command, including the name of the theater
     @param remoteServer the remote server object used to communicate with the server
     @throws RemoteException if there is an error communicating with the remote server
     */
    private static void handleListCommand(String[] args, TheaterRMIService remoteServer) throws RemoteException {
        if (args.length != 3) {
            System.out.println(ERROR_MESSAGE);
            System.exit(1);
        }
        String listCommandTheaterName = args[2];
        if (!listCommandTheaterName.equalsIgnoreCase(THEATER_NAME)) {
            System.out.println(ERROR_MESSAGE);
            System.out.println("Theater name is '" + ConsoleColors.GREEN + THEATER_NAME + ConsoleColors.RESET+"'");
            System.exit(1);
        }
        String hostname = args[1];
        if (!isValidHostname(hostname)) {
            System.out.println(ConsoleColors.RED + "Invalid hostname: " + ConsoleColors.RESET + hostname);
            System.exit(1);
        }
        System.out.println("Welcome, " + hostname + "!\n");
        System.out.print(remoteServer.list());
    }

    /**
     Handles the "book" command for the theater reservation program.
     Attempts to book the specified number of seats of the given seat type for the specified guest name
     by calling the remote server's "book" method. If the booking is successful, a success message is printed
     to the console. If the booking fails because there are no seats available or there is an error with the server,
     an error message is printed to the console.
     @param args the input arguments for the "book" command: the hostname of the machine, the type of seats, the number of seats to reserve,
     and the name of the guest
     @param remoteServer the remote server object to call the "book" method on
     @throws RemoteException if there is an error communicating with the remote server
     */
    private static void handleBookCommand(String[] args, TheaterRMIService remoteServer) throws RemoteException {
        validateInput(args);
        String hostname = args[1];
        System.out.println("Welcome, "+ConsoleColors.PURPLE + hostname + ConsoleColors.RESET + "!\n");
        String guestName = args[4];
        SeatType seatType = getSeatType(args[2]);
        int numberOfSeats = Integer.parseInt(args[3]);

        BigDecimal totalPrice = remoteServer.calculatePrice(seatType, numberOfSeats);
        boolean success = remoteServer.book(seatType, numberOfSeats, guestName);

        if (success) {
            System.out.println(ConsoleColors.GREEN + "Successfully booked " + ConsoleColors.RESET + numberOfSeats + " " + seatType + " seats in name "
                    + guestName + " for a total price of " + totalPrice + "€");
        } else {
            Map<SeatType, Integer> availabilityMap = remoteServer.getAvailability();
            int availableSeats = availabilityMap.getOrDefault(seatType,0);
            if (availableSeats == 0) {
                System.out.println(ConsoleColors.RED + "Sorry, there are no " + seatType + " seats available." + ConsoleColors.RESET);
                System.out.println("Would you like to be added to the waiting list for " + seatType + " seats? (y/n)");
                Scanner scanner = new Scanner(System.in);
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("y")) {
                    remoteServer.addToWaitingList(seatType, guestName, numberOfSeats);
                    System.out.println(ConsoleColors.GREEN + "You have been added to the waiting list for " + seatType + " seats." + ConsoleColors.RESET);
                }
            } else {
                System.out.println(ConsoleColors.RED + "Failed to book " + numberOfSeats + " " + seatType + " seats" + ConsoleColors.RESET +". Please try again.");
                if (!remoteServer.getWaitingList(seatType).isEmpty()) {
                    System.out.println("There are clients waiting for " + seatType + " seats. Would you like to be added to the waiting list? (y/n)");
                    Scanner scanner = new Scanner(System.in);
                    boolean validInput = false;
                    while (!validInput){
                        String answer = scanner.next();
                        if (answer.equalsIgnoreCase("y")) {
                            remoteServer.addToWaitingList(seatType,guestName, numberOfSeats);
                            System.out.println(ConsoleColors.GREEN +"You have been added to the waiting list for " + seatType + " seats." + ConsoleColors.RESET);
                            validInput = true;
                        }else if(answer.equalsIgnoreCase("n")){
                            validInput = true;
                        }else{
                            System.out.println("Invalid Input. Please enter 'y' or 'n'");
                        }
                    }
                }
            }
        }
    }

    /**
     Handles the "guests" command, which prints the number of people attending the show and information about each guest.
     @param args the command arguments
     @param remoteServer the remote server to communicate with
     @throws RemoteException if there is an error communicating with the remote server
     */
    private static void handleGuestsCommand(String[] args, TheaterRMIService remoteServer) throws RemoteException {
        if (args.length != 2) {
            System.out.println(ERROR_MESSAGE);
            System.exit(1);
        }
        String hostname = args[1];
        if (!isValidHostname(hostname)) {
            System.out.println(ConsoleColors.RED + "Invalid hostname: "+ ConsoleColors.RESET + hostname);
            System.exit(1);
        }
        System.out.println("Welcome, "+ConsoleColors.PURPLE + hostname + ConsoleColors.RESET + "!\n");
        System.out.println(remoteServer.guests());
    }

    /**
     Handles the "cancel" command to cancel a reservation.
     Validates the input arguments, cancels the reservation on the remote server,
     and prints the result of the cancellation to the console.
     The program expects 5 arguments: the name of the option, the hostname of the machine,
     the type of seats to cancel, the number of seats to cancel,
     and the name of the client who made the reservation.
     If the input is invalid, an error message is printed and the program exits.
     @param args the input arguments for the cancel command
     @param remoteServer the remote TheaterRMIService object to cancel the reservation on
     @throws RemoteException if a remote communication error occurs while canceling the reservation
     */
    private static void handleCancelCommand(String[] args, TheaterRMIService remoteServer) throws RemoteException {
        validateInput(args);
        String guestName = args[4];
        SeatType seatType = getSeatType(args[2]);
        int numberOfSeats = Integer.parseInt(args[3]);
        System.out.println("Welcome, "+ConsoleColors.PURPLE + args[1] + ConsoleColors.RESET + "!\n");

        BigDecimal totalPrice = remoteServer.calculatePrice(seatType, numberOfSeats);
        boolean success = remoteServer.cancel(seatType, numberOfSeats, guestName);

        if (success) {
            System.out.println(ConsoleColors.GREEN + "Successfully canceled " + numberOfSeats + " " + seatType + " seats in name " + guestName + ConsoleColors.RESET);
            System.out.println( totalPrice + "€ will be returned on your bank.");
            String messageForWaitingList = null;
            try{
                messageForWaitingList = remoteServer.notifyGuestsInWaitingList(numberOfSeats,seatType);
            }catch (RemoteException e){
                e.printStackTrace();
            }
            if(messageForWaitingList != null) {
                System.out.println(messageForWaitingList);
            }
        } else {
            System.out.println(ConsoleColors.RED + "Failed to cancel " + numberOfSeats + " " + seatType + " seats"+ConsoleColors.RESET + ". Please try again.");
        }
    }

    /**
     Parses the given string argument to return the corresponding SeatType enum value.
     @param arg the string argument representing a SeatType
     @return the SeatType enum value corresponding to the given string argument
     @throws IllegalArgumentException if the given string argument does not correspond to a valid SeatType
     */
    private static SeatType getSeatType(String arg) {
        return switch (arg.toUpperCase()) {
            case "SA" -> SeatType.SA;
            case "SB" -> SeatType.SB;
            case "SC" -> SeatType.SC;
            case "CE" -> SeatType.CE;
            case "ST" -> SeatType.ST;
            default -> throw new IllegalArgumentException("Invalid seat type: " + arg);
        };
    }

    /**
     * Validates the input arguments for the theater reservation program.
     * The program expects 5 arguments: the name of the option, the type of seats,the hostname of the machine
     * the number of seats to reserve (a positive integer),and the name of the Client.
     * If the input arguments are not valid, the method prints an error message to the console and exits the program.
     * @param args the input arguments to validate
     * @throws IllegalArgumentException if the input is invalid
     */
    private static void validateInput(String[] args) {
        if (args.length != 5) {
            System.out.println(ERROR_MESSAGE);
            System.exit(1);
        }
        String hostname = args[1];
        if (!isValidHostname(hostname)) {
            System.out.println(ConsoleColors.RED + "Invalid hostname: " + ConsoleColors.RESET + hostname);
            System.exit(1);
        }
        List<SeatType> seatTypes = Arrays.asList(SeatType.SA, SeatType.SB, SeatType.SC, SeatType.CE, SeatType.ST);
        if(!seatTypes.contains(getSeatType(args[2].toUpperCase()))){
            System.out.println(ConsoleColors.RED + "Invalid input:" + ConsoleColors.RESET +  "seat type must be one of: " + seatTypes  );
            for(SeatType s : seatTypes){
                System.out.print(s);
            }
            System.exit(1);
        }
        String numberOfSeatsStr = args[3];
        if (!numberOfSeatsStr.matches("\\d+")) {
            System.out.println(ConsoleColors.RED + "Invalid input: number of seats must be a positive integer." + ConsoleColors.RESET);
            System.exit(1);
        }
        int numberOfSeats = Integer.parseInt(numberOfSeatsStr);
        // Check if number of seats is less or equal to 0
        if (numberOfSeats <= 0) {
            System.out.println(ConsoleColors.RED + "Invalid input: number of seats must be a positive integer." + ConsoleColors.RESET);
            System.exit(1);
        }
    }

    /**
     Checks if the given hostname is valid by attempting to resolve it to an InetAddress object.
     A hostname is considered invalid if it cannot be resolved or if its IP address and hostname do not match.
     @param hostname the hostname to be validated
     @return true if the hostname is valid, false otherwise
     */
    public static boolean isValidHostname(String hostname) {
        try {
            InetAddress address = InetAddress.getByName(hostname);
            // If they are not equal, it means that the hostname is valid.
            return !address.getHostAddress().equals(hostname);
        } catch (UnknownHostException e) {
            System.err.println(ConsoleColors.RED + "Unknown host: " + ConsoleColors.RESET + hostname);
            return false;
        }
    }
}
