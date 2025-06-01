package at.fhtw.tourplanner.service.exception;

public class LogNotFoundException extends RuntimeException {
    public LogNotFoundException(Long id) {
        super("Tour-log not found: " + id);
    }
}
