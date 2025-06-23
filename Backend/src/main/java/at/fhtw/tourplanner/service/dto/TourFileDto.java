package at.fhtw.tourplanner.service.dto;

import java.util.List;

public record TourFileDto(
        Long id,
        String name,
        String description,
        String startLocation,
        String endLocation,
        String transportType,
        Double distance,
        String estimatedTime,
        String mapImagePath,
        Integer popularity,
        Double childFriendliness,
        List<Log> logs) {

    public record Log(
            String logTime,
            String comment,
            Integer difficulty,
            Double totalDistance,
            String totalTime,
            Integer rating) {}
}
