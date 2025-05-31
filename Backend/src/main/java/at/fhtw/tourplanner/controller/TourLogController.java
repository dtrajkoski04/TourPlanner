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
    public ResponseEntity<TourLogDto> get(@PathVariable Long tourId, @PathVariable Long logId) {
        TourLogDto dto = logService.getLog(tourId, logId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<TourLogDto> create(@PathVariable Long tourId, @RequestBody TourLogDto dto) {
        return ResponseEntity.ok(logService.createLog(tourId, dto));
    }

    @PutMapping("/{logId}")
    public ResponseEntity<TourLogDto> update(@PathVariable Long tourId, @PathVariable Long logId, @RequestBody TourLogDto dto) {
        TourLogDto updated = logService.updateLog(tourId, logId, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> delete(@PathVariable Long tourId, @PathVariable Long logId) {
        logService.deleteLog(tourId, logId);
        return ResponseEntity.noContent().build();
    }
}
