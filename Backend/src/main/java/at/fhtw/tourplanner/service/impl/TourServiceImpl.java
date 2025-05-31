package at.fhtw.tourplanner.service.impl;


import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.service.dto.TourDto;
import at.fhtw.tourplanner.service.mapper.TourMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

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
        // TODO: HTTP‑Client aufrufen, JSON parsen, Fehler behandeln …
        double dummyDistanceKm = 12.34;
        String dummyDuration = "00:25:00";
        return new RouteInfo(dummyDistanceKm, dummyDuration);
    }

    private record RouteInfo(double distance, String estimatedTime) {}
}
