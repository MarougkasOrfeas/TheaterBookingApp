# TheaterBookingApp
The Theater Booking Application is a simple client-server application that allows users to book seats in a theater. The application uses RMI (Remote Method Invocation) for communication between the client and the server.

## Introduction
The Theater Booking Application consists of a client component and a server component. The client component allows users to connect to a remote server using RMI and perform various actions such as listing available seats, booking seats for a guest, retrieving a list of guests who have booked seats, and canceling a booking.

The server component provides the necessary functionality for handling client requests. It exposes an RMI interface that clients can invoke to perform the desired actions. The server maintains the state of the theater, including the availability of seats and the list of booked seats.

## Features
The Theater Booking Application provides the following features:

* Listing all available seats in the theater.
* Booking specific seats for a guest.
* Retrieving a list of guests who have booked seats.
* Canceling a booking for a given guest and seat number.
* Handling waiting lists for fully booked seat types.
* Calculating the total price of a booking.

## Usage
To use the Theater Booking Application, follow the steps below:

* Start the server by running the TheaterServer class.
* Run the client by executing the TheaterClient class with the desired command-line arguments.
* Follow the instructions provided by the application to interact with the server.

## Client
The client component (TheaterClient) is responsible for connecting to the remote server and executing user commands. It uses RMI to communicate with the server and performs actions based on the provided command-line arguments.

The available commands that can be executed by the client are:

* list: Lists all available seats in the theater.
* book: Books a specific number of seats for a guest.
* guests: Retrieves a list of guests who have booked seats.
* cancel: Cancels a booking for a given guest and seat number.
The client validates the input arguments, communicates with the server using RMI, and prints the results or error messages to the console.

## Server
The server component (TheaterServer) is responsible for handling client requests and maintaining the state of the theater. It provides an RMI interface (TheaterRMIService) that defines the methods that clients can invoke.

The server uses an implementation of the TheaterService interface (TheaterServiceImpl) to handle the client requests. The implementation is initialized with a TheaterDAO object (TheaterDAOImpl), which is responsible for accessing the underlying data.

The server binds the remote TheaterService object to a URL using the Naming class and registers it with the RMI registry on the specified port.

## Dependencies
The Theater Booking Application has the following dependencies:

* Java RMI: The application uses RMI for communication between the client and the server.
* Lombok: Lombok is a library for Java that helps reduce boilerplate code by automatically generating getters, setters, constructors, and other common methods. In the Theater Booking Application, Lombok is used to generate getters and setters for the various model classes, reducing the amount of manual coding required.
