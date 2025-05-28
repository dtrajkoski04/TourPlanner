package at.fhtw.tourplanner.service.mapper;


import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.service.dto.TourDto;
import org.springframework.stereotype.Component;

@Component
public class TourMapper {

    public TourDto toDto(Tour entity) {
        return TourDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .startLocation(entity.getStartLocation())
                .endLocation(entity.getEndLocation())
                .transportType(entity.getTransportType())
                .distance(entity.getDistance())
                .estimatedTime(entity.getEstimatedTime())
                .mapImagePath(entity.getMapImagePath())
                .popularity(entity.getPopularity())
                .childFriendliness(entity.getChildFriendliness())
                .build();
    }

    public Tour toEntity(TourDto dto) {
        return Tour.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .startLocation(dto.getStartLocation())
                .endLocation(dto.getEndLocation())
                .transportType(dto.getTransportType())
                .distance(dto.getDistance())
                .estimatedTime(dto.getEstimatedTime())
                .mapImagePath(dto.getMapImagePath())
                .popularity(dto.getPopularity())
                .childFriendliness(dto.getChildFriendliness())
                .build();
    }
}
