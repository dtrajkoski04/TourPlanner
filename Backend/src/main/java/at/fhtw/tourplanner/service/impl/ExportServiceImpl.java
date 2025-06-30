package at.fhtw.tourplanner.service.impl;

import at.fhtw.tourplanner.service.dto.TourFileDto;
import at.fhtw.tourplanner.persistence.repository.TourLogRepository;
import at.fhtw.tourplanner.persistence.repository.TourRepository;
import at.fhtw.tourplanner.service.*;
import at.fhtw.tourplanner.service.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 *  • Export = direkt aus den Repositories, schnell & vollständig <br>
 *  • Import = nutzt die bestehenden Services → dieselben Validierungen und
 *    ORS-Aufrufe wie bei manuellen API-POSTs
 */
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final TourRepository    tourRepo;   // nur für Export
    private final TourLogRepository logRepo;    // nur für Export

    private final TourService       tourService;  // für Import-Validierung
    private final TourLogService    logService;   // ─┘
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /* -------------------------------------------------------------- */
    /* EXPORT – alle Tours + Logs als JSON                            */
    /* -------------------------------------------------------------- */
    @Override
    public byte[] exportAllTours() {
        List<TourFileDto> file = tourRepo.findAll().stream()
                .map(t -> new TourFileDto(
                        t.getId(), t.getName(), t.getDescription(),
                        t.getStartLocation(), t.getEndLocation(), t.getTransportType(),
                        t.getDistance(), t.getEstimatedTime(), t.getMapImagePath(),
                        t.getPopularity(), t.getChildFriendliness(),
                        t.getTourLogs().stream().map(l ->
                                        new TourFileDto.Log(
                                                l.getLogTime().toString(), l.getComment(),
                                                l.getDifficulty(), l.getTotalDistance(),
                                                l.getTotalTime(), l.getRating()))
                                .toList()))
                .toList();
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(file);
        } catch (Exception e) {
            throw new RuntimeException("Export failed", e);
        }
    }

    /* ------------------------------------------------------------------ */
    /* IMPORT  – erkennt PDF oder JSON                                     */
    /* ------------------------------------------------------------------ */
    @Override
    @Transactional
    public void importTours(byte[] data) {

        if (isPdf(data)) {
            importFromPdf(data);     // Tour-Report.pdf
        } else {
            importFromJson(data);    // altes JSON-Format
        }
    }

    /* ----------  PDF erkennen ---------------------------------------- */
    private static boolean isPdf(byte[] bytes) {
        return bytes.length >= 4 &&
                bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
    }

    /* ------------------------------------------------------------------ */
    /* JSON-Import                                          */
    /* ------------------------------------------------------------------ */
    private void importFromJson(byte[] json) {
        try {
            List<TourFileDto> list = MAPPER.readValue(json, new TypeReference<>() {});

            for (TourFileDto src : list) {
                TourDto saved = tourService.createTour(toTourDto(src));
                for (TourFileDto.Log lg : src.logs())
                    logService.createLog(saved.getId(), toLogDto(lg));
            }

        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }

    /* ------------------------------------------------------------------ */
    /* PDF-Import                                                         */
    /* ------------------------------------------------------------------ */
    private void importFromPdf(byte[] pdf) {
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdf))) {

            String text = new PDFTextStripper().getText(doc);
            List<String> lines = Stream.of(text.split("\\R"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            /* ---- Kopfzeile ---------------------------------------- */
            String titleLine = lines.stream()
                    .filter(l -> l.startsWith("Tour Report"))
                    .findFirst().orElseThrow();
            String tourName = titleLine.replace("Tour Report –", "").trim();

            /* ---- Grunddaten -------------------------------------- */
            int idxInfo = indexOf(lines, "From To Transport Distance Est.");
            String infoLine = lines.get(idxInfo + 1);
            String[] infoTok = infoLine.split("\\s+");
            String from = infoTok[0];
            String to   = infoTok[1];
            String transport = infoTok[2];
            double dist = Double.parseDouble(infoTok[3]);
            String estTime = infoTok[5];                    // "88:06:18"

            TourDto saved = tourService.createTour(TourDto.builder()
                    .name(tourName).startLocation(from).endLocation(to)
                    .transportType(transport).description("Imported from PDF")
                    .build());

            Long tourId = saved.getId();

            /* ---- Logs -------------------------------------------- */
            int idxLogs = indexOf(lines, "Date Diff Km Time Rating");
            for (int i = idxLogs + 1; i < lines.size(); i++) {
                String l = lines.get(i);
                if (l.startsWith("Popularity")) break;      // Ende erreicht

                String[] tok = l.split("\\s+", 6);
                if (tok.length < 6) continue;               // ggf. leere Zeile überspringen

                TourLogDto log = TourLogDto.builder()
                        .logTime(tok[0] + "T00:00:00")
                        .difficulty(Integer.parseInt(tok[1]))
                        .totalDistance(Double.parseDouble(tok[2]))
                        .totalTime(tok[3])
                        .rating(Integer.parseInt(tok[4]))
                        .comment(tok[5])
                        .build();

                logService.createLog(tourId, log);
            }

        } catch (Exception e) {
            throw new RuntimeException("PDF import failed: " + e.getMessage(), e);
        }
    }

    /* ---------- helpers ----------------------------------------------- */
    private static int indexOf(List<String> list, String contains) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).contains(contains)) return i;
        throw new IllegalStateException("Pattern not found: " + contains);
    }

    private static TourDto toTourDto(TourFileDto src) {
        return TourDto.builder()
                .name(src.name()).description(src.description())
                .startLocation(src.startLocation()).endLocation(src.endLocation())
                .transportType(src.transportType())
                .build();
    }
    private static TourLogDto toLogDto(TourFileDto.Log l) {
        return TourLogDto.builder()
                .logTime(l.logTime()).comment(l.comment())
                .difficulty(l.difficulty()).totalDistance(l.totalDistance())
                .totalTime(l.totalTime()).rating(l.rating()).build();
    }
}
