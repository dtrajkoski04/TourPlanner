package at.fhtw.tourplanner.service.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String location) {
        super("Could not geocode location: " + location);
    }
}
