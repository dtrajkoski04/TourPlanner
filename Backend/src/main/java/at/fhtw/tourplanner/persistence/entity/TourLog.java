package at.fhtw.tourplanner.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TourLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;   // Datum & Uhrzeit der Tour

    private String comment;
    private Integer difficulty;      // z. B. 1–5
    @Column(name = "total_distance") private Double totalDistance; // km
    @Column(name = "total_time")     private String totalTime;     // hh:mm:ss
    private Integer rating;          // 1–5 Sterne
}