package gr.uniwa.marou.model;

/**
 * Enum representing console colors for text output.
 */
public enum ConsoleColors {
    RED("\033[0;31m"),
    GREEN("\033[0;32m"),
    PURPLE("\033[0;35m"),
    RESET("\033[0m");

    private final String code;

    ConsoleColors(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
