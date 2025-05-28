package at.fhtw.tourplanner.service.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDto {
    private Long id;
    private String name;
    private String description;
    private String startLocation;
    private String endLocation;
    private String transportType;
    private Double distance;
    private String estimatedTime;
    private String mapImagePath;
    private Integer popularity;
    private Double childFriendliness;
}
