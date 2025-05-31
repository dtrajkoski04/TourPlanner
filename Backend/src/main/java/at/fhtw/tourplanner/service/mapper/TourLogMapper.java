package at.fhtw.tourplanner.service.mapper;

import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.service.dto.TourLogDto;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class TourLogMapper {
    public TourLogDto toDto(TourLog entity) {
        return TourLogDto.builder()
                .id(entity.getId())
                .tourId(entity.getTour().getId())
                .logTime(entity.getLogTime().toString())
                .comment(entity.getComment())
                .difficulty(entity.getDifficulty())
                .totalDistance(entity.getTotalDistance())
                .totalTime(entity.getTotalTime())
                .rating(entity.getRating())
                .build();
    }

    public TourLog toEntity(TourLogDto dto, Tour tour) {
        return TourLog.builder()
                .id(dto.getId())
                .tour(tour)
                .logTime(dto.getLogTime() != null ? LocalDateTime.parse(dto.getLogTime()) : LocalDateTime.now())
                .comment(dto.getComment())
                .difficulty(dto.getDifficulty())
                .totalDistance(dto.getTotalDistance())
                .totalTime(dto.getTotalTime())
                .rating(dto.getRating())
                .build();
    }
}
