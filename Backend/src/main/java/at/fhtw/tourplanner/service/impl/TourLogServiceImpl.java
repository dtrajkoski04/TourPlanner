package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.persistence.repository.TourLogRepository;
import at.fhtw.tourplanner.service.TourLogService;
import at.fhtw.tourplanner.service.dto.TourLogDto;
import at.fhtw.tourplanner.service.mapper.TourLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class TourLogServiceImpl implements TourLogService {
    private final TourLogRepository logRepo;
    private final TourRepository tourRepo;
    private final TourLogMapper mapper;

    @Override
    public List<TourLogDto> getLogsByTourId(Long tourId) {
        return logRepo.findByTourId(tourId).stream().map(mapper::toDto).toList();
    }

    @Override
    public TourLogDto getLog(Long tourId, Long logId) {
        return logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override @Transactional
    public TourLogDto createLog(Long tourId, TourLogDto dto) {
        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + tourId));
        TourLog entity = mapper.toEntity(dto, tour);
        TourLog saved = logRepo.save(entity);
        return mapper.toDto(saved);
    }

    @Override @Transactional
    public TourLogDto updateLog(Long tourId, Long logId, TourLogDto dto) {
        return logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .map(existing -> {
                    existing.setComment(dto.getComment());
                    existing.setDifficulty(dto.getDifficulty());
                    existing.setRating(dto.getRating());
                    existing.setTotalDistance(dto.getTotalDistance());
                    existing.setTotalTime(dto.getTotalTime());
                    if (dto.getLogTime() != null) {
                        existing.setLogTime(LocalDateTime.parse(dto.getLogTime()));
                    }
                    return mapper.toDto(logRepo.save(existing));
                }).orElse(null);
    }

    @Override
    public void deleteLog(Long tourId, Long logId) {
        logRepo.findById(logId)
                .filter(l -> l.getTour().getId().equals(tourId))
                .ifPresent(logRepo::delete);
    }
}
