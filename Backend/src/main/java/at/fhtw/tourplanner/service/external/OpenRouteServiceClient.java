package at.fhtw.tourplanner.service.external;

import at.fhtw.tourplanner.service.exception.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper around OpenRouteService endpoints.
 * Converts ORS error codes → domain exceptions.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class OpenRouteServiceClient {

    private static final String ORS_API_KEY    = "5b3ce3597851110001cf62483063a6d6ecb84897af3c00aa9a0c7e4a";
    private static final String GEOCODE_URL    = "https://api.openrouteservice.org/geocode/search";
    private static final String DIRECTIONS_URL = "https://api.openrouteservice.org/v2/directions/";

    private final RestTemplate rest = new RestTemplate();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern CODE_RX = Pattern.compile("\"code\"\\s*:\\s*(\\d+)");

    /* ------------------------------------------------------------------ */
    /*  PUBLIC                                                            */
    /* ------------------------------------------------------------------ */

    public RouteInfo fetchRouteInfo(String start, String end, String transportType) {

        double[] from = geocode(start);
        double[] to   = geocode(end);

        String profile = mapProfile(transportType);
        String url     = DIRECTIONS_URL + profile;

        HttpEntity<String> entity =
                new HttpEntity<>(buildBody(from, to), orsHeaders());

        Map<String, Object> resp = safePost(url, entity, start, end);

        List<?> routes = (List<?>) resp.get("routes");
        if (routes == null || routes.isEmpty())
            throw new RouteNotFoundException(start, end);

        Map<String, Object> summary =
                (Map<String, Object>) ((Map<?, ?>) routes.get(0)).get("summary");

        double km   = ((Number) summary.get("distance")).doubleValue() / 1000.0;
        long   secs = ((Number) summary.get("duration")).longValue();

        return new RouteInfo(km, formatDuration(secs));
    }

    /* ------------------------------------------------------------------ */
    /*  GEOCODING                                                         */
    /* ------------------------------------------------------------------ */

    private double[] geocode(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(GEOCODE_URL)
                .queryParam("api_key", ORS_API_KEY)
                .queryParam("text", address)
                .queryParam("size", 1)
                .build()
                .toUriString();

        Map<String, Object> resp = safeGet(url);
        List<?> feats = (List<?>) resp.get("features");
        if (feats == null || feats.isEmpty())
            throw new LocationNotFoundException(address);

        Map<String, ?> geom = (Map<String, ?>) ((Map<?, ?>) feats.get(0)).get("geometry");
        List<?> coords      = (List<?>) geom.get("coordinates"); // [lon, lat]

        return new double[]{
                ((Number) coords.get(0)).doubleValue(),
                ((Number) coords.get(1)).doubleValue()
        };
    }

    /* ------------------------------------------------------------------ */
    /*  TRANSPORT TYPE                                                    */
    /* ------------------------------------------------------------------ */

    private String mapProfile(String type) {
        if (type == null) throw new InvalidTransportTypeException("null");
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "cycling", "bike", "bicycle", "cycling-regular" -> "cycling-regular";
            case "foot", "walking", "hiking", "foot-walking"     -> "foot-walking";
            case "driving-car", "car", "auto"                   -> "driving-car";
            default -> throw new InvalidTransportTypeException(type);
        };
    }

    /* ------------------------------------------------------------------ */
    /*  SAFE HTTP WRAPPERS                                                */
    /* ------------------------------------------------------------------ */

    private Map<String, Object> safeGet(String url) {
        try {
            return rest.getForObject(url, Map.class);

        } catch (HttpClientErrorException.TooManyRequests ex) {
            throw new ExternalServiceException("ORS rate limit exceeded (HTTP 429)");

        } catch (RestClientException ex) {
            throw new ExternalServiceException("ORS error: " + ex.getMessage());
        }
    }

    private Map<String, Object> safePost(String url,
                                         HttpEntity<?> entity,
                                         String start,
                                         String end) {

        try {
            return rest.postForObject(url, entity, Map.class);

            /* ---------- ALL 4xx FROM ORS ----------------------------------- */
        } catch (HttpClientErrorException ex) {
            int code = extractErrorCode(ex.getResponseBodyAsString());

            /* 2010  →  one of the Coordinates is not on a routable path  */
            if (code == 2010) {
                boolean atStart = ex.getResponseBodyAsString().contains("coordinate 0");
                throw new LocationNotFoundException(atStart ? start : end);
            }

            /* 2003  NO Route   |  2008  Distance > 6 000 km   | 2009 too many points found */
            if (code == 2003 || code == 2008 || code == 2009) {
                throw new RouteNotFoundException(start, end);
            }

            if (code == 2070) {
                throw new InvalidTransportTypeException("profile");
            }

            /* jedes andere 400/404 von /directions behandeln wir als „keine Route“ */
            if (ex.getStatusCode().is4xxClientError()) {
                throw new RouteNotFoundException(start, end);
            }

            /* all others as a ExternalServiceException */
            throw new ExternalServiceException("ORS " + ex.getStatusCode() + ": " + ex.getStatusText());

            /* ---------- Netzwerk / 5xx ------------------------------------ */
        } catch (RestClientException ex) {
            throw new ExternalServiceException("ORS error: " + ex.getMessage());
        }
    }


    /* ------------------------------------------------------------------ */
    /*  JSON / REGEX helper                                               */
    /* ------------------------------------------------------------------ */

    private static int extractErrorCode(String body) {
        // try proper JSON first
        try {
            JsonNode root = MAPPER.readTree(body);
            return root.path("error").path("code").asInt(-1);
        } catch (Exception ignore) { /* fall through */ }

        // fallback: regex
        Matcher m = CODE_RX.matcher(body);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    /* ------------------------------------------------------------------ */
    /*  UTILITIES                                                         */
    /* ------------------------------------------------------------------ */

    private static HttpHeaders orsHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", ORS_API_KEY);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private static String buildBody(double[] s, double[] e) {
        return String.format(
                Locale.US,
                "{\"coordinates\":[[%f,%f],[%f,%f]]}",
                s[0], s[1], e[0], e[1]);
    }

    private static String formatDuration(long seconds) {
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /* ------------------------------------------------------------------ */
    /*  DTO                                                               */
    /* ------------------------------------------------------------------ */
    public record RouteInfo(double distanceKm, String duration) {}
}
