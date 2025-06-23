package at.fhtw.tourplanner;

import at.fhtw.tourplanner.service.dto.TourLogDto;
import at.fhtw.tourplanner.service.exception.LogValidationException;
import at.fhtw.tourplanner.service.impl.TourLogServiceImpl;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TourLogServiceImplValidationTest {
    TourLogServiceImpl service;

    @BeforeEach void setUp() { service = new TourLogServiceImpl(null, null, null); }

    // Use reflection to access private validate method
    void validate(TourLogDto dto, boolean allRequired) throws Exception {
        Method m = TourLogServiceImpl.class.getDeclaredMethod("validate", TourLogDto.class, boolean.class);
        m.setAccessible(true);
        m.invoke(service, dto, allRequired);
    }

    @Test void validate_logTimeRequired_missing_throws() {
        TourLogDto dto = new TourLogDto();
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause() instanceof LogValidationException);
    }

    @Test void validate_logTimeFormat_invalid_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("bad-format");
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("logTime must be ISO-8601"));
    }

    @Test void validate_difficultyRequired_missing_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00");
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("difficulty is required"));
    }

    @Test void validate_difficulty_outOfRange_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00"); dto.setDifficulty(0);
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("difficulty must be between 1 and 5"));
    }

    @Test void validate_ratingRequired_missing_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00"); dto.setDifficulty(3);
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("rating is required"));
    }

    @Test void validate_rating_outOfRange_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00"); dto.setDifficulty(3); dto.setRating(6);
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("rating must be between 1 and 5"));
    }

    @Test void validate_totalDistanceRequired_missing_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00"); dto.setDifficulty(3); dto.setRating(4);
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("totalDistance is required"));
    }

    @Test void validate_totalDistance_negative_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00"); dto.setDifficulty(3); dto.setRating(4); dto.setTotalDistance(-5.0);
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("totalDistance must be positive"));
    }

    @Test void validate_totalTimeRequired_missing_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00"); dto.setDifficulty(3); dto.setRating(4); dto.setTotalDistance(10.0);
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("totalTime is required"));
    }

    @Test void validate_totalTimeFormat_invalid_throws() {
        TourLogDto dto = new TourLogDto(); dto.setLogTime("2024-01-01T12:00:00"); dto.setDifficulty(3); dto.setRating(4); dto.setTotalDistance(10.0); dto.setTotalTime("bad");
        Exception ex = assertThrows(Exception.class, () -> validate(dto, true));
        assertTrue(ex.getCause().getMessage().contains("totalTime must be HH:mm:ss"));
    }
}