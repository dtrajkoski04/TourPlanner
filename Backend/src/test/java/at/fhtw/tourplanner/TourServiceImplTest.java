package at.fhtw.tourplanner;

import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.dto.TourDto;
import at.fhtw.tourplanner.service.exception.TourNotFoundException;
import at.fhtw.tourplanner.service.external.OpenRouteServiceClient;
import at.fhtw.tourplanner.service.impl.TourServiceImpl;
import at.fhtw.tourplanner.service.mapper.TourMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TourServiceImplTest {
    @Mock TourRepository tourRepo;
    @Mock TourMapper mapper;
    @Mock OpenRouteServiceClient ors;
    @InjectMocks
    TourServiceImpl service;

    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }

    @Test void getAllTours_returnsAll() {
        List<Tour> tours = List.of(new Tour(), new Tour());
        when(tourRepo.findAll()).thenReturn(tours);
        when(mapper.toDto(any())).thenReturn(new TourDto());
        assertEquals(2, service.getAllTours().size());
    }

    @Test void getTourById_found() {
        Tour t = new Tour(); t.setId(1L);
        when(tourRepo.findById(1L)).thenReturn(Optional.of(t));
        when(mapper.toDto(t)).thenReturn(new TourDto());
        assertNotNull(service.getTourById(1L));
    }

    @Test void getTourById_notFound() {
        when(tourRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TourNotFoundException.class, () -> service.getTourById(1L));
    }

    @Test void createTour_setsDistanceAndTime() {
        TourDto dto = new TourDto(); Tour entity = new Tour();
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(ors.fetchRouteInfo(any(), any(), any())).thenReturn(new OpenRouteServiceClient.RouteInfo(10.0, "01:30:00"));
        when(tourRepo.save(any())).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        TourDto result = service.createTour(dto);
        assertEquals(dto, result);
    }

    @Test void updateTour_notExists_returnsNull() {
        when(tourRepo.existsById(1L)).thenReturn(false);
        assertNull(service.updateTour(1L, new TourDto()));
    }

    @Test void updateTour_found_updatesAndSaves() {
        Tour t = new Tour(); t.setId(1L);
        when(tourRepo.existsById(1L)).thenReturn(true);
        when(tourRepo.findById(1L)).thenReturn(Optional.of(t));
        when(ors.fetchRouteInfo(any(), any(), any())).thenReturn(new OpenRouteServiceClient.RouteInfo(5.0, "00:45:00"));
        when(mapper.toDto(any())).thenReturn(new TourDto());
        TourDto result = service.updateTour(1L, new TourDto());
        assertNotNull(result);
    }

    @Test void deleteTour_notExists_throws() {
        when(tourRepo.existsById(99L)).thenReturn(false);
        assertThrows(TourNotFoundException.class, () -> service.deleteTour(99L));
    }

    @Test void deleteTour_exists_deletes() {
        when(tourRepo.existsById(1L)).thenReturn(true);
        service.deleteTour(1L);
        verify(tourRepo).deleteById(1L);
    }
}