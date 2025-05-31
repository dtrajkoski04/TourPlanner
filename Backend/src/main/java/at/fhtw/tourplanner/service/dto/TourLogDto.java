package at.fhtw.tourplanner.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TourLogDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long tourId;

    private String logTime;       // ISO‑8601 z. B. "2025-05-31T14:20:00"
    private String comment;
    private Integer difficulty;   // 1–5
    private Double totalDistance; // km
    private String totalTime;     // hh:mm:ss
    private Integer rating;       // 1–5
}