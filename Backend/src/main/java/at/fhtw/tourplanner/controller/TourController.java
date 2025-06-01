package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.service.dto.TourDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping
    public List<TourDto> getAllTours() {
        return tourService.getAllTours();
    }

    @GetMapping("/{id}")
    public TourDto getTour(@PathVariable Long id) {
        return tourService.getTourById(id);      // 404 handled by exception
    }

    @PostMapping
    public TourDto createTour(@RequestBody TourDto dto) {
        return tourService.createTour(dto);
    }

    @PutMapping("/{id}")
    public TourDto updateTour(@PathVariable Long id, @RequestBody TourDto dto) {
        return tourService.updateTour(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);              // throws 404 if missing
    }
}
