package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.service.dto.TourLogDto;
import java.util.List;

public interface TourLogService {
    List<TourLogDto> getLogsByTourId(Long tourId);
    TourLogDto getLog(Long tourId, Long logId);
    TourLogDto createLog(Long tourId, TourLogDto dto);
    TourLogDto updateLog(Long tourId, Long logId, TourLogDto dto);
    void deleteLog(Long tourId, Long logId);
}
