package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.service.ExportService;
import at.fhtw.tourplanner.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final ExportService exportService;
    private final ReportService reportService;

    /* ----------- import / export --------------------------------- */

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportJson() {
        byte[] data = exportService.exportAllTours();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tours.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void importJson(@RequestBody byte[] file) {
        exportService.importTours(file);
    }

    /* ------------- reports --------------------------------------- */

    @GetMapping("/report/tour/{id}")
    public ResponseEntity<byte[]> tourReport(@PathVariable Long id) {
        byte[] md = reportService.generateTourReport(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"tour-" + id + ".md\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(md);
    }

    @GetMapping("/report/summary")
    public ResponseEntity<byte[]> summaryReport() {
        byte[] md = reportService.generateSummaryReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"summary.md\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(md);
    }
}
