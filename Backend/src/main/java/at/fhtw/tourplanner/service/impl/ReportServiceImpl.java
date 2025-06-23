package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.persistence.entity.Tour;
import at.fhtw.tourplanner.persistence.entity.TourLog;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.ReportService;
import at.fhtw.tourplanner.service.exception.TourNotFoundException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.IntSummaryStatistics;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TourRepository tourRepo;

    /* ------------------------------------------------------------------ */
    /*  SINGLE TOUR REPORT                                                */
    /* ------------------------------------------------------------------ */
    @Override
    public byte[] generateTourReport(Long id) {

        Tour t = tourRepo.findById(id).orElseThrow(() -> new TourNotFoundException(id));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            /* Titel & Beschreibung ---------------------------------- */
            doc.add(new Paragraph("Tour Report – " + t.getName(), h1));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(t.getDescription(), normal));
            doc.add(Chunk.NEWLINE);

            /* Überblickstabelle ------------------------------------- */
            PdfPTable info = new PdfPTable(5);
            info.setWidthPercentage(100);
            addHeader(info, "From"); addHeader(info, "To");
            addHeader(info, "Transport"); addHeader(info, "Distance");
            addHeader(info, "Est. Time");

            info.addCell(t.getStartLocation());
            info.addCell(t.getEndLocation());
            info.addCell(t.getTransportType());
            info.addCell(String.format(Locale.US, "%.2f km", t.getDistance()));
            info.addCell(t.getEstimatedTime());
            doc.add(info);

            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Popularity: " + t.getPopularity(), normal));
            doc.add(new Paragraph("Child-friendliness: " + t.getChildFriendliness(), normal));
            doc.add(Chunk.NEWLINE);

            /* Log-Tabelle ------------------------------------------- */
            doc.add(new Paragraph("Logs", h2));
            PdfPTable logs = new PdfPTable(6);
            logs.setWidthPercentage(100);
            addHeader(logs, "Date"); addHeader(logs, "Diff");
            addHeader(logs, "Km");   addHeader(logs, "Time");
            addHeader(logs, "Rating"); addHeader(logs, "Comment");

            for (TourLog l : t.getTourLogs()) {
                logs.addCell(l.getLogTime().toLocalDate().toString());
                logs.addCell(l.getDifficulty().toString());
                logs.addCell(String.format(Locale.US, "%.2f", l.getTotalDistance()));
                logs.addCell(l.getTotalTime());
                logs.addCell(l.getRating().toString());
                logs.addCell(l.getComment() == null ? "" : l.getComment());
            }
            doc.add(logs);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  SUMMARY REPORT                                                    */
    /* ------------------------------------------------------------------ */
    @Override
    public byte[] generateSummaryReport() {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            doc.add(new Paragraph("Tour Summary Report", h1));
            doc.add(Chunk.NEWLINE);

            PdfPTable tbl = new PdfPTable(5);
            tbl.setWidthPercentage(100);
            addHeader(tbl, "Tour");
            addHeader(tbl, "Logs");
            addHeader(tbl, "Ø Km");
            addHeader(tbl, "Ø Time");
            addHeader(tbl, "Ø Rating");

            for (Tour t : tourRepo.findAll()) {

                /* rating Ø ------------------------------------------ */
                IntSummaryStatistics rs = t.getTourLogs().stream()
                        .filter(l -> l.getRating() != null)
                        .mapToInt(TourLog::getRating).summaryStatistics();
                double avgRating = rs.getCount() == 0 ? 0 : rs.getAverage();

                /* distance Ø ---------------------------------------- */
                double avgKm = t.getTourLogs().stream()
                        .mapToDouble(TourLog::getTotalDistance).average().orElse(0);

                /* time Ø -------------------------------------------- */
                long avgSec = (long) t.getTourLogs().stream()
                        .mapToLong(l -> LocalTime.parse(l.getTotalTime()).toSecondOfDay())
                        .average().orElse(0);
                String avgTime = String.format("%02d:%02d:%02d",
                        avgSec / 3600, (avgSec % 3600) / 60, avgSec % 60);

                /* rows ---------------------------------------------- */
                tbl.addCell(t.getName());
                tbl.addCell(String.valueOf(t.getTourLogs().size()));
                tbl.addCell(String.format(Locale.US, "%.2f", avgKm));
                tbl.addCell(avgTime);
                tbl.addCell(String.format(Locale.US, "%.1f", avgRating));
            }

            doc.add(tbl);
            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  helper                                                            */
    /* ------------------------------------------------------------------ */
    private static void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        cell.setGrayFill(0.85f);
        table.addCell(cell);
    }
}
