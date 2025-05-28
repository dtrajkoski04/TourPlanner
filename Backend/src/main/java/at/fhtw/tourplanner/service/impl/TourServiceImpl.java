package at.fhtw.tourplanner.service.impl;


import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.service.dto.TourDto;
import at.fhtw.tourplanner.service.mapper.TourMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    @Override
    public List<TourDto> getAllTours() {
        return tourRepository.findAll()
                .stream()
                .map(tourMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TourDto getTourById(Long id) {
        return tourRepository.findById(id)
                .map(tourMapper::toDto)
                .orElse(null);
    }

    @Override
    public TourDto createTour(TourDto dto) {
        return tourMapper.toDto(tourRepository.save(tourMapper.toEntity(dto)));
    }

    @Override
    public TourDto updateTour(Long id, TourDto dto) {
        if (!tourRepository.existsById(id)) return null;
        Tour updated = tourMapper.toEntity(dto);
        updated.setId(id);
        return tourMapper.toDto(tourRepository.save(updated));
    }

    @Override
    public void deleteTour(Long id) {
        tourRepository.deleteById(id);
    }
}
