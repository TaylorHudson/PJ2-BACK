package br.com.pj2.back.dataprovider.database.adapter;

import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfGeneratorAdapterTest {

    @Mock
    private MonitoringSessionAdapter monitoringSessionAdapter;

    @InjectMocks
    private PdfGeneratorAdapter pdfGeneratorAdapter;

    @Test
    void shouldGeneratePdfWhenMonitorFulfillsFullWorkload() {
        when(monitoringSessionAdapter.getWorkedHoursByMonth("123", 7, 2026))
                .thenReturn(Duration.ofHours(40));

        File pdfFile = pdfGeneratorAdapter.generatePdf("123", 7, 2026);

        assertNotNull(pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);

        verify(monitoringSessionAdapter)
                .getWorkedHoursByMonth("123", 7, 2026);

        pdfFile.delete();
    }

    @Test
    void shouldGeneratePdfWhenMonitorDoesNotFulfillFullWorkload() {
        when(monitoringSessionAdapter.getWorkedHoursByMonth("123", 7, 2026))
                .thenReturn(Duration.ofHours(35));

        File pdfFile = pdfGeneratorAdapter.generatePdf("123", 7, 2026);

        assertNotNull(pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);

        verify(monitoringSessionAdapter)
                .getWorkedHoursByMonth("123", 7, 2026);

        pdfFile.delete();
    }

    @Test
    void shouldGeneratePdfWithHoursAndMinutes() {
        when(monitoringSessionAdapter.getWorkedHoursByMonth("123", 7, 2026))
                .thenReturn(Duration.ofHours(32).plusMinutes(45));

        File pdfFile = pdfGeneratorAdapter.generatePdf("123", 7, 2026);

        assertNotNull(pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);

        verify(monitoringSessionAdapter)
                .getWorkedHoursByMonth("123", 7, 2026);

        pdfFile.delete();
    }

    @Test
    void shouldThrowBadRequestExceptionWhenErrorOccurs() {
        when(monitoringSessionAdapter.getWorkedHoursByMonth("789", 7, 2026))
                .thenThrow(new RuntimeException("Erro de banco"));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> pdfGeneratorAdapter.generatePdf("789", 7, 2026)
        );

        assertEquals(ErrorCode.SERVER_ERROR, ex.getErrorCode());

        verify(monitoringSessionAdapter)
                .getWorkedHoursByMonth("789", 7, 2026);
    }
}
