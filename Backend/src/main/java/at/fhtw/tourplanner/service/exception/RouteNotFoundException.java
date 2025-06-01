package at.fhtw.tourplanner.service.exception;

public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(String start, String end) {
        super("No routable path between " + start + " and " + end);
    }
}