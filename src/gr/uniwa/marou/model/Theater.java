package gr.uniwa.marou.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;


/**
 The Theater class represents a theater with a set of seat types, prices and availability for each type.
 It also contains the theater's name.
 */
@Getter
@Setter
public class Theater implements Serializable {

    private final Map<SeatType, BigDecimal>prices;
    private final Map<SeatType, Integer>availability;

    private final String theaterName;

    /**
     Constructs a new Theater object with default values for prices, availability and name.
     */
    public Theater(){

        prices = new HashMap<>();
        availability = new HashMap<>();
        this.theaterName = "MyTheater";

        try (Scanner scanner =  new Scanner(Objects.requireNonNull(getClass().getResourceAsStream("theater_seats.csv")))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                SeatType seatType = SeatType.valueOf(parts[0]);
                BigDecimal price = new BigDecimal(parts[1]);
                int availableSeats = Integer.parseInt(parts[2]);

                prices.put(seatType, price);
                availability.put(seatType, availableSeats);
            }
        } catch (Exception e) {
            System.out.println("File Not Found " + e.getMessage());
        }

        /* Hardcoded approach

        prices.put(SeatType.SA, new BigDecimal("45.00"));
        prices.put(SeatType.SB, new BigDecimal("35.00"));
        prices.put(SeatType.SC, new BigDecimal("25.00"));
        prices.put(SeatType.CE, new BigDecimal("30.00"));
        prices.put(SeatType.ST, new BigDecimal("20.00"));

        availability.put(SeatType.SA, 100);
        availability.put(SeatType.SB, 200);
        availability.put(SeatType.SC, 400);
        availability.put(SeatType.CE, 225);
        availability.put(SeatType.ST, 75);

        */
    }

}
