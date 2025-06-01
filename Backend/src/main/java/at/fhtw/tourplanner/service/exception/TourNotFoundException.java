package at.fhtw.tourplanner.service.exception;

public class TourNotFoundException extends RuntimeException {
    public TourNotFoundException(Long id) {
        super("Tour not found: " + id);
    }
}

