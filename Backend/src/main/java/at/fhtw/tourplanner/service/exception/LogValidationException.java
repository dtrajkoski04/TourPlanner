package at.fhtw.tourplanner.service.exception;

/** Thrown when any TourLog field value is invalid. */
public class LogValidationException extends RuntimeException {
    public LogValidationException(String msg) {
        super(msg);
    }
}
