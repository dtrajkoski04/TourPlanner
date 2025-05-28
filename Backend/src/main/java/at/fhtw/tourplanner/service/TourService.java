package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.service.dto.TourDto;

import java.util.List;

public interface TourService {
    List<TourDto> getAllTours();
    TourDto getTourById(Long id);
    TourDto createTour(TourDto tourDto);
    TourDto updateTour(Long id, TourDto tourDto);
    void deleteTour(Long id);
}
