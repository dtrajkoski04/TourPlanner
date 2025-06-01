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
import at.fhtw.tourplanner.service.exception.TourNotFoundException;
import at.fhtw.tourplanner.service.exception.LogNotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourLogServiceImpl implements TourLogService {

    private final TourLogRepository logRepo;
    private final TourRepository    tourRepo;
    private final TourLogMapper     mapper;

    /* ------------------------------------------------------------ */
    /*  READ                                                        */
    /* ------------------------------------------------------------ */
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

    /* ------------------------------------------------------------ */
    /*  CREATE                                                      */
    /* ------------------------------------------------------------ */
    @Override @Transactional
    public TourLogDto createLog(Long tourId, TourLogDto dto) {

        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException(tourId));

        validate(dto, true);                       // all required on create
        TourLog entity = mapper.toEntity(dto, tour);

        return mapper.toDto(logRepo.save(entity));
    }

    /* ------------------------------------------------------------ */
    /*  UPDATE                                                      */
    /* ------------------------------------------------------------ */
    @Override @Transactional
    public TourLogDto updateLog(Long tourId, Long logId, TourLogDto dto) {

        TourLog existing = logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .orElseThrow(() -> new LogNotFoundException(logId));

        validate(dto, false);                    // partial allowed

        if (dto.getComment()       != null) existing.setComment(dto.getComment());
        if (dto.getDifficulty()    != null) existing.setDifficulty(dto.getDifficulty());
        if (dto.getRating()        != null) existing.setRating(dto.getRating());
        if (dto.getTotalDistance() != null) existing.setTotalDistance(dto.getTotalDistance());
        if (dto.getTotalTime()     != null) existing.setTotalTime(dto.getTotalTime());
        if (dto.getLogTime()       != null) existing.setLogTime(LocalDateTime.parse(dto.getLogTime()));

        return mapper.toDto(logRepo.save(existing));
    }

    /* ------------------------------------------------------------ */
    /*  DELETE                                                      */
    /* ------------------------------------------------------------ */
    @Override
    public void deleteLog(Long tourId, Long logId) {
        TourLog log = logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .orElseThrow(() -> new LogNotFoundException(logId));
        logRepo.delete(log);
    }

    /* ------------------------------------------------------------ */
    /*  VALIDATION                                                  */
    /* ------------------------------------------------------------ */
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

        // totalTime hh:mm:ss
        if (allRequired && dto.getTotalTime() == null)
            throw new LogValidationException("totalTime is required");

        if (dto.getTotalTime() != null) {
            try {
                LocalTime.parse(dto.getTotalTime());
            } catch (DateTimeParseException e) {
                throw new LogValidationException("totalTime must be HH:mm:ss");
            }
        }
    }
}
