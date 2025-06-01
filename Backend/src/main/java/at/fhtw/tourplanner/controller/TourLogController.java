package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.service.TourLogService;
import at.fhtw.tourplanner.service.dto.TourLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tours/{tourId}/logs")
@RequiredArgsConstructor
public class TourLogController {
    private final TourLogService logService;

    @GetMapping
    public List<TourLogDto> list(@PathVariable Long tourId) {
        return logService.getLogsByTourId(tourId);
    }

    @GetMapping("/{logId}")
    public TourLogDto get(@PathVariable Long tourId, @PathVariable Long logId) {
        return logService.getLog(tourId, logId);
    }

    @PostMapping
    public TourLogDto create(@PathVariable Long tourId, @RequestBody TourLogDto dto) {
        return logService.createLog(tourId, dto);
    }

    @PutMapping("/{logId}")
    public TourLogDto update(@PathVariable Long tourId, @PathVariable Long logId,
                             @RequestBody TourLogDto dto) {
        return logService.updateLog(tourId, logId, dto);
    }

    @DeleteMapping("/{logId}")
    public void delete(@PathVariable Long tourId, @PathVariable Long logId) {
        logService.deleteLog(tourId, logId);
    }
}
