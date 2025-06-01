package at.fhtw.tourplanner.web;

import at.fhtw.tourplanner.service.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(InvalidTransportTypeException.class)
    public ResponseEntity<ApiError> handleTransport(InvalidTransportTypeException ex,
                                                    HttpServletRequest req) {
        return build(ex, HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ApiError> handleLocation(LocationNotFoundException ex,
                                                   HttpServletRequest req) {
        return build(ex, HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<ApiError> handleRoute(RouteNotFoundException ex,
                                                HttpServletRequest req) {
        return build(ex, HttpStatus.NOT_FOUND, req);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiError> handleExternal(ExternalServiceException ex,
                                                   HttpServletRequest req) {
        return build(ex, HttpStatus.SERVICE_UNAVAILABLE, req);
    }

    /* fallback */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex,
                                                  HttpServletRequest req) {
        return build(ex, HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    private ResponseEntity<ApiError> build(Exception ex, HttpStatus status, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI()));
    }
}
