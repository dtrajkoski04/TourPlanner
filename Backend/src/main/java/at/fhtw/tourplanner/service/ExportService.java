package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.service.dto.TourFileDto;
import java.util.List;

public interface ExportService {
    byte[] exportAllTours();                     // JSON bytes
    void   importTours(byte[] json);             // merge / override
}
