package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.persistence.repository.TourLogRepository;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.TourLogService;
import at.fhtw.tourplanner.service.dto.TourLogDto;
import at.fhtw.tourplanner.service.exception.*;
import at.fhtw.tourplanner.service.mapper.TourLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class TourLogServiceImpl implements TourLogService {

    private final TourLogRepository logRepo;
    private final TourRepository    tourRepo;
    private final TourLogMapper     mapper;

    /* ------------------------------------------------------------------ */
    /* READ                                                               */
    /* ------------------------------------------------------------------ */
    @Override
    public List<TourLogDto> getLogsByTourId(Long tourId) {
        if (!tourRepo.existsById(tourId))
            throw new TourNotFoundException(tourId);
        return logRepo.findByTourId(tourId).stream().map(mapper::toDto).toList();
    }

    @Override
    public TourLogDto getLog(Long tourId, Long logId) {
        TourLog log = logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .orElseThrow(() -> new LogNotFoundException(logId));
        return mapper.toDto(log);
    }

    /* ------------------------------------------------------------------ */
    /* CREATE                                                             */
    /* ------------------------------------------------------------------ */
    @Override @Transactional
    public TourLogDto createLog(Long tourId, TourLogDto dto) {

        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException(tourId));

        validate(dto, true);

        TourLog saved = logRepo.save(mapper.toEntity(dto, tour));

        recalcAggregates(tour);                     // ▼ update popularity & child-friendliness
        tourRepo.save(tour);

        return mapper.toDto(saved);
    }

    /* ------------------------------------------------------------------ */
    /* UPDATE                                                             */
    /* ------------------------------------------------------------------ */
    @Override @Transactional
    public TourLogDto updateLog(Long tourId, Long logId, TourLogDto dto) {

        TourLog existing = logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .orElseThrow(() -> new LogNotFoundException(logId));

        validate(dto, false);

        if (dto.getComment()       != null) existing.setComment(dto.getComment());
        if (dto.getDifficulty()    != null) existing.setDifficulty(dto.getDifficulty());
        if (dto.getRating()        != null) existing.setRating(dto.getRating());
        if (dto.getTotalDistance() != null) existing.setTotalDistance(dto.getTotalDistance());
        if (dto.getTotalTime()     != null) existing.setTotalTime(dto.getTotalTime());
        if (dto.getLogTime()       != null) existing.setLogTime(LocalDateTime.parse(dto.getLogTime()));

        TourLog saved = logRepo.save(existing);

        recalcAggregates(existing.getTour());
        tourRepo.save(existing.getTour());

        return mapper.toDto(saved);
    }

    /* ------------------------------------------------------------------ */
    /* DELETE                                                             */
    /* ------------------------------------------------------------------ */
    @Override
    @Transactional
    public void deleteLog(Long tourId, Long logId) {
        TourLog log = logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .orElseThrow(() -> new LogNotFoundException(logId));

        Tour parent = log.getTour();
        logRepo.delete(log);

        recalcAggregates(parent);
        tourRepo.save(parent);
    }

    /* ------------------------------------------------------------------ */
    /* VALIDATION                                                         */
    /* ------------------------------------------------------------------ */
    private void validate(TourLogDto dto, boolean allRequired) {

        // logTime
        if (allRequired && dto.getLogTime() == null)
            throw new LogValidationException("logTime is required");
        if (dto.getLogTime() != null) {
            try { LocalDateTime.parse(dto.getLogTime()); }
            catch (DateTimeParseException e) {
                throw new LogValidationException("logTime must be ISO-8601 (yyyy-MM-ddTHH:mm:ss)");
            }
        }

        // difficulty 1-5
        if (allRequired && dto.getDifficulty() == null)
            throw new LogValidationException("difficulty is required");
        if (dto.getDifficulty() != null &&
                (dto.getDifficulty() < 1 || dto.getDifficulty() > 5))
            throw new LogValidationException("difficulty must be between 1 and 5");

        // rating 1-5
        if (allRequired && dto.getRating() == null)
            throw new LogValidationException("rating is required");
        if (dto.getRating() != null &&
                (dto.getRating() < 1 || dto.getRating() > 5))
            throw new LogValidationException("rating must be between 1 and 5");

        // totalDistance ≥ 0
        if (allRequired && dto.getTotalDistance() == null)
            throw new LogValidationException("totalDistance is required");
        if (dto.getTotalDistance() != null && dto.getTotalDistance() < 0)
            throw new LogValidationException("totalDistance must be positive");

        // totalTime HH:mm:ss
        if (allRequired && dto.getTotalTime() == null)
            throw new LogValidationException("totalTime is required");
        if (dto.getTotalTime() != null) {
            try { LocalTime.parse(dto.getTotalTime()); }
            catch (DateTimeParseException e) {
                throw new LogValidationException("totalTime must be HH:mm:ss");
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* AGGREGATE CALCULATION                                              */
    /* ------------------------------------------------------------------ */
    private void recalcAggregates(Tour tour) {

        List<TourLog> logs = logRepo.findByTourId(tour.getId());
        int logCount = logs.size();

        if (logCount == 0) {
            tour.setPopularity(0);
            tour.setChildFriendliness(null);
            return;
        }

        /* -------- popularity = logCount × avgRating ------------------- */
        OptionalDouble ratingAvg = logs.stream()
                .filter(l -> l.getRating() != null)
                .mapToInt(TourLog::getRating)
                .average();

        double pop = logCount * ratingAvg.orElse(0);
        tour.setPopularity((int) Math.round(pop));

        /* -------- child-friendliness ---------------------------------- */
        double avgDiff = logs.stream().mapToInt(TourLog::getDifficulty).average().orElse(5);
        double avgDist = logs.stream().mapToDouble(TourLog::getTotalDistance).average().orElse(0);
        double avgSec  = logs.stream().mapToLong(l -> toSeconds(l.getTotalTime())).average().orElse(0);

        double diffScore     = 6 - avgDiff;                              // 1→5, 5→1
        double distanceScore = scale(avgDist,   5, 30);                  // 5-→1
        double timeScore     = scale(avgSec/3600.0, 1, 8);               // hours

        double childFriendly = (diffScore + distanceScore + timeScore) / 3.0;
        tour.setChildFriendliness(Math.round(childFriendly * 10) / 10.0); // one decimal
    }

    /** linear 5-to-1 score between bestLimit and worstLimit */
    private static double scale(double value, double bestLimit, double worstLimit) {
        if (value <= bestLimit)  return 5;
        if (value >= worstLimit) return 1;
        double ratio = (value - bestLimit) / (worstLimit - bestLimit);   // 0-1
        return 5 - ratio * 4;                                            // 5-→1
    }

    private static long toSeconds(String hhmmss) {
        LocalTime t = LocalTime.parse(hhmmss);
        return t.toSecondOfDay();
    }
}
