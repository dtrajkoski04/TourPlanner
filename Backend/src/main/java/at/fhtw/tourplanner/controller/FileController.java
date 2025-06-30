package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.service.ExportService;
import at.fhtw.tourplanner.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @PostMapping(value = "/import",
            consumes = { MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_PDF_VALUE })
    public void importRaw(@RequestBody byte[] body) {
        exportService.importTours(body);          // JSON  oder  raw-PDF
    }

    /* ------------- multipart -------------------- */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importMultipart(@RequestParam("file") MultipartFile file) throws IOException {
        exportService.importTours(file.getBytes());   // PDF aus multipart
    }

    /* ------------- reports --------------------------------------- */

    @GetMapping("/report/tour/{id}")
    public ResponseEntity<byte[]> tourReport(@PathVariable Long id) {
        byte[] pdf = reportService.generateTourReport(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"tour-"+id+".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/report/summary")
    public ResponseEntity<byte[]> summaryReport() {
        byte[] pdf = reportService.generateSummaryReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"summary.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
