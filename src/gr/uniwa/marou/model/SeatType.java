package gr.uniwa.marou.model;

/**
 An enum representing the different types of seats in a theater.
 */
public enum SeatType {
    SA("Square - Zone A"),
    SB("Square - Zone B"),
    SC("Square - Zone C"),
    CE("Central Extractor"),
    ST("Side Theorems");

    private final String name;

    SeatType(String name){
        this.name =name;
    }

    @Override
    public String toString() {
        return name;
    }
}
