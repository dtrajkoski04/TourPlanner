package at.fhtw.tourplanner;

import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.persistence.repository.TourLogRepository;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.dto.TourLogDto;
import at.fhtw.tourplanner.service.exception.*;
import at.fhtw.tourplanner.service.impl.TourLogServiceImpl;
import at.fhtw.tourplanner.service.mapper.TourLogMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TourLogServiceImplTest {
    @Mock TourLogRepository logRepo;
    @Mock TourRepository tourRepo;
    @Mock TourLogMapper mapper;
    @InjectMocks
    TourLogServiceImpl service;

    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }

    @Test void getLogsByTourId_exists() {
        when(tourRepo.existsById(1L)).thenReturn(true);
        when(logRepo.findByTourId(1L)).thenReturn(List.of(new TourLog(), new TourLog()));
        when(mapper.toDto(any())).thenReturn(new TourLogDto());
        assertEquals(2, service.getLogsByTourId(1L).size());
    }

    @Test void getLogsByTourId_notExist_throws() {
        when(tourRepo.existsById(99L)).thenReturn(false);
        assertThrows(TourNotFoundException.class, () -> service.getLogsByTourId(99L));
    }

    @Test void getLog_found() {
        Tour t = new Tour(); t.setId(1L);
        TourLog log = new TourLog(); log.setTour(t);
        when(logRepo.findById(2L)).thenReturn(Optional.of(log));
        when(mapper.toDto(log)).thenReturn(new TourLogDto());
        assertNotNull(service.getLog(1L, 2L));
    }

    @Test void getLog_wrongTour_throws() {
        Tour t = new Tour(); t.setId(2L);
        TourLog log = new TourLog(); log.setTour(t);
        when(logRepo.findById(2L)).thenReturn(Optional.of(log));
        assertThrows(LogNotFoundException.class, () -> service.getLog(1L, 2L));
    }

    @Test void createLog_validatesSavesAndReturnsDto() {
        Tour t = new Tour(); t.setId(1L);
        TourLogDto dto = new TourLogDto();
        dto.setLogTime("2024-01-01T12:00:00");
        dto.setDifficulty(3);
        dto.setRating(4);
        dto.setTotalDistance(10.0);
        dto.setTotalTime("01:30:00");
        TourLog log = new TourLog();

        when(tourRepo.findById(1L)).thenReturn(Optional.of(t));
        when(mapper.toEntity(dto, t)).thenReturn(log);
        when(logRepo.save(log)).thenReturn(log);
        when(mapper.toDto(log)).thenReturn(dto);

        TourLogDto result = service.createLog(1L, dto);
        assertEquals(dto, result);
    }

    @Test void createLog_tourNotFound_throws() {
        when(tourRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TourNotFoundException.class, () -> service.createLog(99L, new TourLogDto()));
    }

    @Test void updateLog_found_updatesAndReturnsDto() {
        Tour t = new Tour(); t.setId(1L);
        TourLog log = new TourLog(); log.setTour(t);
        TourLogDto dto = new TourLogDto();

        when(logRepo.findById(5L)).thenReturn(Optional.of(log));
        when(logRepo.save(log)).thenReturn(log);
        when(mapper.toDto(log)).thenReturn(dto);

        TourLogDto result = service.updateLog(1L, 5L, dto);
        assertEquals(dto, result);
    }

    @Test void updateLog_notFound_throws() {
        when(logRepo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(LogNotFoundException.class, () -> service.updateLog(1L, 5L, new TourLogDto()));
    }

    @Test void updateLog_wrongTour_throws() {
        Tour t = new Tour(); t.setId(2L);
        TourLog log = new TourLog(); log.setTour(t);
        when(logRepo.findById(5L)).thenReturn(Optional.of(log));
        assertThrows(LogNotFoundException.class, () -> service.updateLog(1L, 5L, new TourLogDto()));
    }

    @Test void deleteLog_found_deletes() {
        Tour t = new Tour(); t.setId(1L);
        TourLog log = new TourLog(); log.setTour(t);

        when(logRepo.findById(5L)).thenReturn(Optional.of(log));
        // logRepo.delete(log) is void, so no need for doNothing()
        // tourRepo.save(t) is void too

        service.deleteLog(1L, 5L);
        verify(logRepo).delete(log);
        verify(tourRepo).save(t);
    }

    @Test void deleteLog_notFound_throws() {
        when(logRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LogNotFoundException.class, () -> service.deleteLog(1L, 99L));
    }
}