package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.ReportService;
import at.fhtw.tourplanner.service.exception.TourNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TourRepository tourRepo;

    @Override
    public byte[] generateTourReport(Long id) {
        Tour t = tourRepo.findById(id).orElseThrow(() -> new TourNotFoundException(id));
        StringBuilder md = new StringBuilder("# Tour Report – ").append(t.getName()).append("\n\n")
                .append("**Description:** ").append(t.getDescription()).append("\n\n")
                .append("| From | To | Transport | Distance | Est. Time |\n")
                .append("|------|----|-----------|----------|-----------|\n")
                .append("| ").append(t.getStartLocation()).append(" | ")
                .append(t.getEndLocation()).append(" | ")
                .append(t.getTransportType()).append(" | ")
                .append(t.getDistance()).append(" km | ")
                .append(t.getEstimatedTime()).append(" |\n\n")
                .append("**Popularity:** ").append(t.getPopularity()).append("<br>")
                .append("**Child-friendliness:** ").append(t.getChildFriendliness()).append("\n\n")
                .append("## Logs\n")
                .append("| Date | Diff | Dist (km) | Time | Rating | Comment |\n")
                .append("|------|------|-----------|------|--------|---------|\n");
        t.getTourLogs().forEach(l -> md.append("| ")
                .append(l.getLogTime().toLocalDate()).append(" | ")
                .append(l.getDifficulty()).append(" | ")
                .append(l.getTotalDistance()).append(" | ")
                .append(l.getTotalTime()).append(" | ")
                .append(l.getRating()).append(" | ")
                .append(l.getComment() == null ? "" : l.getComment().replace("|","\\|")).append(" |\n"));
        return md.toString().getBytes();
    }

    @Override
    public byte[] generateSummaryReport() {
        StringBuilder md = new StringBuilder("# Tour Summary Report\n\n")
                .append("| Tour | Logs | Avg km | Avg hh:mm:ss | Avg rating |\n")
                .append("|------|------|--------|--------------|------------|\n");

        tourRepo.findAll().forEach(t -> {
            /* rating average ---------------------------------------- */
            IntSummaryStatistics ratingStats = t.getTourLogs().stream()
                    .filter(l -> l.getRating()!=null)
                    .mapToInt(TourLog::getRating)
                    .summaryStatistics();
            double avgRating = ratingStats.getCount() > 0
                    ? ratingStats.getAverage()
                    : 0.0;

            /* distance average -------------------------------------- */
            double avgKm = t.getTourLogs().stream()
                    .mapToDouble(TourLog::getTotalDistance)
                    .average().orElse(0);

            /* time average ------------------------------------------ */
            long avgSec = (long) t.getTourLogs().stream()
                    .mapToLong(l -> LocalTime.parse(l.getTotalTime()).toSecondOfDay())
                    .average().orElse(0);
            String avgTime = Duration.ofSeconds(avgSec).toString().substring(2);

            md.append("| ").append(t.getName()).append(" | ")
                    .append(t.getTourLogs().size()).append(" | ")
                    .append(String.format("%.2f", avgKm)).append(" | ")
                    .append(avgTime).append(" | ")
                    .append(String.format("%.1f", avgRating)).append(" |\n");
        });
        return md.toString().getBytes();
    }
}
