package at.fhtw.tourplanner.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "start_location")
    private String startLocation;

    @Column(name = "end_location")
    private String endLocation;

    @Column(name = "transport_type")
    private String transportType;

    private Double distance;

    @Column(name = "estimated_time")
    private String estimatedTime; // Consider using Duration or custom format

    @Column(name = "map_image_path")
    private String mapImagePath;

    private Integer popularity;

    @Column(name = "child_friendliness")
    private Double childFriendliness;
}
