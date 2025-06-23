package at.fhtw.tourplanner.service;

public interface ReportService {
    /** Markdown bytes for one tour incl. logs */
    byte[] generateTourReport(Long tourId);

    /** Markdown bytes summarizing all tours */
    byte[] generateSummaryReport();
}
