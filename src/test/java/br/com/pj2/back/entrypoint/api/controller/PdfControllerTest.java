package br.com.pj2.back.entrypoint.api.controller;

import br.com.pj2.back.core.usecase.PdfGeneratorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PdfControllerTest {
    private static final String AUTH_HEADER = "Bearer token";

    @Mock
    private PdfGeneratorUseCase pdfGeneratorUseCase;
    @InjectMocks
    private PdfController pdfController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(pdfController).build();
    }

    @Test
    void shouldReturnPdfFileSuccessfully() throws Exception {
        File tempPdf = File.createTempFile("test", ".pdf");
        Files.write(tempPdf.toPath(), new byte[]{1, 2, 3});

        when(pdfGeneratorUseCase.execute(anyString(), anyInt(), anyInt()))
                .thenReturn(tempPdf);

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        mockMvc.perform(post("/pdf/month-workloads")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        String.format(
                                "attachment; filename=\"declaracao_carga_horaria_%02d-%d.pdf\"",
                                currentMonth,
                                currentYear
                        )))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(content().bytes(Files.readAllBytes(tempPdf.toPath())));

        verify(pdfGeneratorUseCase)
                .execute(AUTH_HEADER, currentMonth, currentYear);
    }

    @Test
    void shouldUseProvidedMonthAndYear() throws Exception {
        File tempPdf = File.createTempFile("test", ".pdf");
        Files.write(tempPdf.toPath(), new byte[]{1, 2, 3});

        when(pdfGeneratorUseCase.execute(anyString(), anyInt(), anyInt()))
                .thenReturn(tempPdf);

        mockMvc.perform(post("/pdf/month-workloads")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("month", "7")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"declaracao_carga_horaria_07-2026.pdf\""));

        verify(pdfGeneratorUseCase)
                .execute(AUTH_HEADER, 7, 2026);
    }

    @Test
    void shouldReturnInternalServerErrorWhenPdfGenerationFails() throws Exception {
        File tempPdf = File.createTempFile("test", ".pdf");

        when(pdfGeneratorUseCase.execute(anyString(), anyInt(), anyInt()))
                .thenReturn(tempPdf);

        tempPdf.delete();

        mockMvc.perform(post("/pdf/month-workloads")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/pdf/month-workloads"))
                .andExpect(status().is4xxClientError());
    }
}
