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
import java.util.IntSummaryStatistics;
import java.util.Locale;

/**
 * Creates Markdown reports with fixed-width columns,
 * so the table is readable in raw form and
 * renders nicely in any Markdown viewer.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TourRepository tourRepo;

    /* ------------------------------------------------------------ */
    /*  Single-tour report                                          */
    /* ------------------------------------------------------------ */
    @Override
    public byte[] generateTourReport(Long id) {

        Tour t = tourRepo.findById(id).orElseThrow(() -> new TourNotFoundException(id));

        /* --- Head ------------------------------------------------ */
        StringBuilder sb = new StringBuilder()
                .append("# Tour Report – ").append(t.getName()).append("\n\n")
                .append("**Description:** ").append(t.getDescription()).append("\n\n")
                .append("```\n")                                 // monospace block
                .append(pad("From", 15)).append(pad("To", 15))
                .append(pad("Transport", 12)).append(pad("Distance", 10))
                .append("Est.Time").append("\n")
                .append(repeat('-', 65)).append("\n")
                .append(pad(t.getStartLocation(), 15))
                .append(pad(t.getEndLocation(),   15))
                .append(pad(t.getTransportType(), 12))
                .append(String.format(Locale.US,"%8.2fkm ", t.getDistance()))
                .append(t.getEstimatedTime()).append("\n")
                .append("```\n\n")
                .append("> Popularity: ").append(t.getPopularity())
                .append("   |   Child-friendliness: ")
                .append(t.getChildFriendliness()).append("\n\n")
                .append("## Logs\n")
                .append("```\n")
                .append(pad("Date", 12)).append(pad("Diff", 6))
                .append(pad("Km", 8)).append(pad("Time", 10))
                .append(pad("Rating", 8)).append("Comment\n")
                .append(repeat('-', 80)).append("\n");

        /* --- Logs ------------------------------------------------ */
        t.getTourLogs().forEach(l -> sb.append(pad(l.getLogTime().toLocalDate().toString(), 12))
                .append(pad(l.getDifficulty().toString(), 6))
                .append(String.format(Locale.US,"%6.2f  ", l.getTotalDistance()))
                .append(pad(l.getTotalTime(), 10))
                .append(pad(l.getRating().toString(), 8))
                .append(l.getComment() == null ? "" : l.getComment().replace("\n"," "))
                .append("\n"));
        sb.append("```\n");

        return sb.toString().getBytes();
    }

    /* ------------------------------------------------------------ */
    /*  Summary report                                              */
    /* ------------------------------------------------------------ */
    @Override
    public byte[] generateSummaryReport() {

        StringBuilder sb = new StringBuilder("# Tour Summary Report\n\n```\n")
                .append(pad("Tour", 22)).append(pad("Logs", 6))
                .append(pad("Ø Km", 10)).append(pad("Ø Time", 10))
                .append("Ø Rating\n")
                .append(repeat('-', 60)).append("\n");

        tourRepo.findAll().forEach(t -> {

            /* rating average */
            IntSummaryStatistics rs = t.getTourLogs().stream()
                    .filter(l -> l.getRating() != null)
                    .mapToInt(TourLog::getRating).summaryStatistics();
            double avgRating = rs.getCount() == 0 ? 0 : rs.getAverage();

            /* distance average */
            double avgKm = t.getTourLogs().stream()
                    .mapToDouble(TourLog::getTotalDistance).average().orElse(0);

            /* time average */
            long avgSec = (long) t.getTourLogs().stream()
                    .mapToLong(l -> LocalTime.parse(l.getTotalTime()).toSecondOfDay())
                    .average().orElse(0);
            String avgTime = String.format("%02d:%02d:%02d",
                    avgSec / 3600, (avgSec % 3600) / 60, avgSec % 60);

            sb.append(pad(t.getName(), 22))
                    .append(pad(Integer.toString(t.getTourLogs().size()), 6))
                    .append(pad(String.format(Locale.US,"%.2f", avgKm), 10))
                    .append(pad(avgTime, 10))
                    .append(String.format(Locale.US,"%.1f", avgRating)).append("\n");
        });

        sb.append("```\n");
        return sb.toString().getBytes();
    }

    /* ------------------------------------------------------------ */
    /*  Helper                                                      */
    /* ------------------------------------------------------------ */
    private static String pad(String s, int len) {
        return (s.length() >= len)
                ? s.substring(0, len - 1) + " "
                : String.format("%-" + len + "s", s);
    }
    private static String repeat(char c, int n) { return String.valueOf(c).repeat(n); }
}
