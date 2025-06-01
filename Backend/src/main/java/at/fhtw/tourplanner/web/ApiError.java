package at.fhtw.tourplanner.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter @AllArgsConstructor
public class ApiError {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}
