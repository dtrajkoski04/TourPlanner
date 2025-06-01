package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.service.external.OpenRouteServiceClient;
import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.service.dto.TourDto;
import at.fhtw.tourplanner.service.mapper.TourMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository              tourRepo;
    private final TourMapper                  mapper;
    private final OpenRouteServiceClient      ors;          // << injected client

    /* -------------------------------------------------------------- */
    /*  READ                                                          */
    /* -------------------------------------------------------------- */
    @Override
    public List<TourDto> getAllTours() {
        return tourRepo.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public TourDto getTourById(Long id) {
        return tourRepo.findById(id).map(mapper::toDto).orElse(null);
    }

    /* -------------------------------------------------------------- */
    /*  CREATE / UPDATE                                               */
    /* -------------------------------------------------------------- */
    @Override @Transactional
    public TourDto createTour(TourDto dto) {
        Tour entity = mapper.toEntity(dto);

        /* distance & duration come from ORS ------------------------- */
        var info = ors.fetchRouteInfo(entity.getStartLocation(),
                entity.getEndLocation(),
                entity.getTransportType());
        entity.setDistance(info.distanceKm());
        entity.setEstimatedTime(info.duration());
        /* ----------------------------------------------------------- */

        return mapper.toDto(tourRepo.save(entity));
    }

    @Override @Transactional
    public TourDto updateTour(Long id, TourDto dto) {
        if (!tourRepo.existsById(id)) return null;
        Tour entity = mapper.toEntity(dto);
        entity.setId(id);

        var info = ors.fetchRouteInfo(entity.getStartLocation(),
                entity.getEndLocation(),
                entity.getTransportType());
        entity.setDistance(info.distanceKm());
        entity.setEstimatedTime(info.duration());

        return mapper.toDto(tourRepo.save(entity));
    }

    /* -------------------------------------------------------------- */
    /*  DELETE                                                        */
    /* -------------------------------------------------------------- */
    @Override
    public void deleteTour(Long id) {
        tourRepo.deleteById(id);
    }
}
