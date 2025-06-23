package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.service.dto.TourFileDto;
import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.persistence.repository.TourLogRepository;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.ExportService;
import at.fhtw.tourplanner.service.TourLogService;
import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.service.dto.TourDto;
import at.fhtw.tourplanner.service.dto.TourLogDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  • Export = direkt aus den Repositories, schnell & vollständig <br>
 *  • Import = nutzt die bestehenden Services → dieselben Validierungen und
 *    ORS-Aufrufe wie bei manuellen API-POSTs
 */
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final TourRepository    tourRepo;   // nur für Export
    private final TourLogRepository logRepo;    // nur für Export

    private final TourService       tourService;  // für Import-Validierung
    private final TourLogService    logService;   // ─┘
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /* -------------------------------------------------------------- */
    /* EXPORT – alle Tours + Logs als JSON                            */
    /* -------------------------------------------------------------- */
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
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(file);
        } catch (Exception e) {
            throw new RuntimeException("Export failed", e);
        }
    }

    /* -------------------------------------------------------------- */
    /* IMPORT – JSON einlesen, Tour + Logs via Services anlegen       */
    /* -------------------------------------------------------------- */
    @Override
    @Transactional
    public void importTours(byte[] json) {

        try {
            List<TourFileDto> list = MAPPER.readValue(json, new TypeReference<>() {});

            for (TourFileDto src : list) {

                /* 1) Tour anlegen  ---------------------------------- */
                TourDto newTour = TourDto.builder()
                        .name(src.name()).description(src.description())
                        .startLocation(src.startLocation()).endLocation(src.endLocation())
                        .transportType(src.transportType())
                        .build();

                TourDto savedTour = tourService.createTour(newTour);           // ← löst alle Checks aus
                Long tourId = savedTour.getId();

                /* 2) Logs anlegen  ---------------------------------- */
                for (TourFileDto.Log lg : src.logs()) {
                    TourLogDto dto = TourLogDto.builder()
                            .logTime(lg.logTime())
                            .comment(lg.comment())
                            .difficulty(lg.difficulty())
                            .totalDistance(lg.totalDistance())
                            .totalTime(lg.totalTime())
                            .rating(lg.rating())
                            .build();

                    logService.createLog(tourId, dto);                         // ← validiert jeden Log
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }
}
