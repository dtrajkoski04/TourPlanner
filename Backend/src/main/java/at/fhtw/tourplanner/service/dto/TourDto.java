package at.fhtw.tourplanner.service.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String name;
    private String description;
    private String startLocation;
    private String endLocation;
    private String transportType;

    // These are fetched from the openroute API not from the user
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double distance;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String estimatedTime;

    private String mapImagePath;
    private Integer popularity;
    private Double childFriendliness;
}
