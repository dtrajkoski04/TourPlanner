package at.fhtw.tourplanner.service.impl;


import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.service.dto.TourDto;
import at.fhtw.tourplanner.service.mapper.TourMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private static final String ORS_API_KEY = "5b3ce3597851110001cf62483063a6d6ecb84897af3c00aa9a0c7e4a";
    private static final String GEOCODE_URL = "https://api.openrouteservice.org/geocode/search";
    private static final String DIRECTIONS_URL = "https://api.openrouteservice.org/v2/directions/";
    private static final RestTemplate REST = new RestTemplate();

    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    @Override
    public List<TourDto> getAllTours() {
        return tourRepository.findAll()
                .stream()
                .map(tourMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TourDto getTourById(Long id) {
        return tourRepository.findById(id)
                .map(tourMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public TourDto createTour(TourDto dto) {
        Tour entity = tourMapper.toEntity(dto);

        // --- Distanz & Dauer via Routing‑API berechnen ------------------
        RouteInfo routeInfo = fetchRouteInfo(entity.getStartLocation(),
                entity.getEndLocation(),
                entity.getTransportType());
        entity.setDistance(routeInfo.distance());
        entity.setEstimatedTime(routeInfo.estimatedTime());
        // ----------------------------------------------------------------

        Tour saved = tourRepository.save(entity);
        return tourMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TourDto updateTour(Long id, TourDto dto) {
        if (!tourRepository.existsById(id)) return null;
        Tour updated = tourMapper.toEntity(dto);
        updated.setId(id);

        // Distance & Time neu berechnen, falls Strecke verändert wurde
        RouteInfo routeInfo = fetchRouteInfo(updated.getStartLocation(),
                updated.getEndLocation(),
                updated.getTransportType());
        updated.setDistance(routeInfo.distance());
        updated.setEstimatedTime(routeInfo.estimatedTime());

        return tourMapper.toDto(tourRepository.save(updated));
    }

    @Override
    public void deleteTour(Long id) {
        tourRepository.deleteById(id);
    }

    private RouteInfo fetchRouteInfo(String start, String end, String transportType) {
        double[] startCoords = geocode(start);
        double[] endCoords   = geocode(end);

        String profile = mapProfile(transportType);
        String url = DIRECTIONS_URL + profile;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", ORS_API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(Locale.US,
                "{\"coordinates\":[[%f,%f],[%f,%f]]}",
                startCoords[0], startCoords[1], endCoords[0], endCoords[1]);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        Map<String, Object> response = REST.postForObject(url, entity, Map.class);
        if (response == null || response.get("routes") == null) {
            throw new IllegalStateException("No route returned from ORS");
        }
        Map<String, Object> firstRoute = ((List<Map<String, Object>>) response.get("routes")).get(0);
        Map<String, Object> summary = (Map<String, Object>) firstRoute.get("summary");

        double distanceKm = ((Number) summary.get("distance")).doubleValue() / 1000.0; // Meter → km
        long durationSec  = ((Number) summary.get("duration")).longValue();            // Sekunden

        return new RouteInfo(distanceKm, formatDuration(durationSec));
    }

    /**
     * Geocodiert eine Adresse und gibt [Lon,Lat] zurück.
     */
    private double[] geocode(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(GEOCODE_URL)
                .queryParam("api_key", ORS_API_KEY)
                .queryParam("text", address)
                .queryParam("size", 1)
                .build()
                .toUriString();

        Map<String, Object> resp = REST.getForObject(url, Map.class);
        if (resp == null || resp.get("features") == null) {
            throw new IllegalStateException("No geocode result for address: " + address);
        }
        Map<String, Object> feature = ((List<Map<String, Object>>) resp.get("features")).get(0);
        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
        List<Object> coords = (List<Object>) geometry.get("coordinates"); // [lon, lat]
        return new double[]{ ((Number) coords.get(0)).doubleValue(), ((Number) coords.get(1)).doubleValue() };
    }

    /**
     * Mapped freie Transport‑Strings auf ORS‑Profile.
     */
    private String mapProfile(String transportType) {
        if (transportType == null) return "driving-car";
        return switch (transportType.toLowerCase(Locale.ROOT)) {
            case "cycling", "bike", "bicycle", "cycling-regular" -> "cycling-regular";
            case "foot", "walking", "hiking", "foot-walking"     -> "foot-walking";
            default                                                  -> "driving-car";
        };
    }

    private String formatDuration(long seconds) {
        long hrs = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }


    private record RouteInfo(double distance, String estimatedTime) {}
}
