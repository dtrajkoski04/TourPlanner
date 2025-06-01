package at.fhtw.tourplanner.service.exception;

public class InvalidTransportTypeException extends RuntimeException {
    public InvalidTransportTypeException(String type) {
        super("Unsupported transport type: " + type);
    }
}
