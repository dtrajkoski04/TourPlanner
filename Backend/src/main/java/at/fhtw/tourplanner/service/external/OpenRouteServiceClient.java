package at.fhtw.tourplanner.service.external;

import at.fhtw.tourplanner.service.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * Small wrapper around OpenRouteService REST endpoints (geocoding + directions).
 * All network errors / bad inputs are translated into domain-specific RuntimeExceptions.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class OpenRouteServiceClient {

    private static final String ORS_API_KEY = "5b3ce3597851110001cf62483063a6d6ecb84897af3c00aa9a0c7e4a";
    private static final String GEOCODE_URL = "https://api.openrouteservice.org/geocode/search";
    private static final String DIRECTIONS_URL = "https://api.openrouteservice.org/v2/directions/";

    private final RestTemplate rest = new RestTemplate();

    /* ------------------------------------------------------------------ */
    /*  PUBLIC API                                                        */
    /* ------------------------------------------------------------------ */

    /**
     * Returns distance (km) and travel time (hh:mm:ss) between two locations.
     */
    public RouteInfo fetchRouteInfo(String start, String end, String transportType) {
        double[] s = geocode(start);
        double[] e = geocode(end);

        String profile = mapProfile(transportType);
        String url = DIRECTIONS_URL + profile;

        HttpEntity<String> entity = new HttpEntity<>(buildBody(s, e), orsHeaders());
        Map<String, Object> resp;
        try {
            resp = safePost(url, entity);
        } catch (RouteNotFoundException ex) {
            throw new RouteNotFoundException(start, end);
        }

        List<?> routes = (List<?>) resp.get("routes");
        if (routes == null || routes.isEmpty()) {
            throw new RouteNotFoundException(start, end);
        }
        Map<String, Object> summary = (Map<String, Object>) ((Map<?, ?>) routes.get(0)).get("summary");

        double km  = ((Number) summary.get("distance")).doubleValue() / 1000.0;
        long secs  = ((Number) summary.get("duration")).longValue();

        return new RouteInfo(km, formatDuration(secs));
    }

    /* ------------------------------------------------------------------ */
    /*  INTERNAL HELPERS                                                  */
    /* ------------------------------------------------------------------ */

    private double[] geocode(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(GEOCODE_URL)
                .queryParam("api_key", ORS_API_KEY)
                .queryParam("text", address)
                .queryParam("size", 1)
                .build().toUriString();

        Map<String, Object> resp = safeGet(url);
        List<?> feats = (List<?>) resp.get("features");
        if (feats == null || feats.isEmpty()) {
            throw new LocationNotFoundException(address);
        }
        Map<String, ?> geometry = (Map<String, ?>) ((Map<?, ?>) feats.get(0)).get("geometry");
        List<?> coords = (List<?>) geometry.get("coordinates");
        return new double[] {
                ((Number) coords.get(0)).doubleValue(),
                ((Number) coords.get(1)).doubleValue()
        };
    }

    private String mapProfile(String transportType) {
        if (transportType == null) throw new InvalidTransportTypeException("null");
        return switch (transportType.toLowerCase(Locale.ROOT)) {
            case "cycling", "bike", "bicycle", "cycling-regular" -> "cycling-regular";
            case "foot", "walking", "hiking", "foot-walking"     -> "foot-walking";
            case "driving-car", "car", "auto"                   -> "driving-car";
            default -> throw new InvalidTransportTypeException(transportType);
        };
    }

    /* --- low-level wrappers that translate RestTemplate errors -------- */

    private Map<String, Object> safeGet(String url) {
        try {
            return rest.getForObject(url, Map.class);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests ex) {
            throw new ExternalServiceException("ORS rate limit exceeded (HTTP 429)");
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new ExternalServiceException("ORS error: " + ex.getMessage());
        }
    }

    private Map<String, Object> safePost(String url, HttpEntity<?> entity) {
        try {
            return rest.postForObject(url, entity, Map.class);

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new RouteNotFoundException("start-/end-coordinates", "");

        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest ex) {
            throw new RouteNotFoundException("start-/end-coordinates", "");

        } catch (org.springframework.web.client.RestClientException ex) {
            throw new ExternalServiceException("ORS error: " + ex.getMessage());
        }
    }


    private static HttpHeaders orsHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", ORS_API_KEY);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private static String buildBody(double[] s, double[] e) {
        return String.format(Locale.US,
                "{\"coordinates\":[[%f,%f],[%f,%f]]}", s[0], s[1], e[0], e[1]);
    }

    private static String formatDuration(long seconds) {
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /* ------------------------------------------------------------------ */
    /*  DATA CLASS                                                        */
    /* ------------------------------------------------------------------ */
    public record RouteInfo(double distanceKm, String duration) {}
}
