package br.com.pj2.back.entrypoint.api.controller;

import br.com.pj2.back.core.usecase.PdfGeneratorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;

@Tag(name = "PDF com carga horaria mensal")
@RestController
@RequestMapping("/pdf/month-workloads")
@RequiredArgsConstructor
public class PdfController {

    private final PdfGeneratorUseCase pdfGeneratorUseCase;

    @Operation(summary = "Baixar PDF com carga horaria mensal")
    @PostMapping
    public ResponseEntity<byte[]> generateMonthlyTimeLoadPdf(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        LocalDate now = LocalDate.now();
        month = month != null ? month : now.getMonthValue();
        year = year != null ? year : now.getYear();

        File pdf = pdfGeneratorUseCase.execute(authorizationHeader, month, year);
        try {
            byte[] content = Files.readAllBytes(pdf.toPath());
            String filename = String.format(
                    "declaracao_carga_horaria_%02d-%d.pdf",
                    month,
                    year
            );
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
