package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.service.dto.TourFileDto;
import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.persistence.repository.TourLogRepository;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.ExportService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final TourRepository    tourRepo;
    private final TourLogRepository logRepo;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] exportAllTours() {
        List<TourFileDto> file = tourRepo.findAll().stream()
                .map(t -> new TourFileDto(
                        t.getId(), t.getName(), t.getDescription(),
                        t.getStartLocation(), t.getEndLocation(), t.getTransportType(),
                        t.getDistance(), t.getEstimatedTime(), t.getMapImagePath(),
                        t.getPopularity(), t.getChildFriendliness(),
                        t.getTourLogs().stream().map(l ->
                                        new TourFileDto.Log(
                                                l.getLogTime().toString(), l.getComment(),
                                                l.getDifficulty(), l.getTotalDistance(),
                                                l.getTotalTime(), l.getRating()))
                                .toList()))
                .toList();
        try { return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(file); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override @Transactional
    public void importTours(byte[] json) {
        try {
            List<TourFileDto> list = MAPPER.readValue(json, new TypeReference<>(){});
            for (TourFileDto dto : list) {
                Tour tour = Tour.builder()
                        .name(dto.name()).description(dto.description())
                        .startLocation(dto.startLocation()).endLocation(dto.endLocation())
                        .transportType(dto.transportType())
                        .distance(dto.distance()).estimatedTime(dto.estimatedTime())
                        .mapImagePath(dto.mapImagePath())
                        .popularity(dto.popularity()).childFriendliness(dto.childFriendliness())
                        .build();
                Tour saved = tourRepo.save(tour);

                for (TourFileDto.Log l : dto.logs()) {
                    TourLog log = TourLog.builder()
                            .tour(saved)
                            .logTime(LocalDateTime.parse(l.logTime()))
                            .comment(l.comment())
                            .difficulty(l.difficulty())
                            .totalDistance(l.totalDistance())
                            .totalTime(l.totalTime())
                            .rating(l.rating())
                            .build();
                    logRepo.save(log);
                }
            }
        } catch (Exception e) { throw new RuntimeException("Import failed: " + e.getMessage(), e); }
    }
}
