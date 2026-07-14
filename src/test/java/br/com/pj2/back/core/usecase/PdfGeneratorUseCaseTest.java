package br.com.pj2.back.core.usecase;

import br.com.pj2.back.core.gateway.PdfGeneratorGateway;
import br.com.pj2.back.core.gateway.TokenGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfGeneratorUseCaseTest {

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private PdfGeneratorGateway pdfGeneratorGateway;

    @InjectMocks
    private PdfGeneratorUseCase pdfGeneratorUseCase;

    @Test
    void shouldGeneratePdfWhenAuthorizationHeaderIsValid() {
        // Arrange
        String authHeader = "Bearer token";
        String registration = "user123";
        Integer month = 7;
        Integer year = 2026;
        File expectedFile = mock(File.class);

        when(tokenGateway.extractSubjectFromAuthorization(authHeader))
                .thenReturn(registration);

        when(pdfGeneratorGateway.generatePdf(registration, month, year))
                .thenReturn(expectedFile);

        // Act
        File result = pdfGeneratorUseCase.execute(authHeader, month, year);

        // Assert
        assertEquals(expectedFile, result);

        verify(tokenGateway)
                .extractSubjectFromAuthorization(authHeader);

        verify(pdfGeneratorGateway)
                .generatePdf(registration, month, year);
    }

    @Test
    void shouldThrowExceptionWhenAuthorizationHeaderIsInvalid() {
        // Arrange
        Integer month = 7;
        Integer year = 2026;

        when(tokenGateway.extractSubjectFromAuthorization(null))
                .thenThrow(new RuntimeException("Invalid header"));

        // Act / Assert
        assertThrows(
                RuntimeException.class,
                () -> pdfGeneratorUseCase.execute(null, month, year)
        );

        verify(tokenGateway)
                .extractSubjectFromAuthorization(null);

        verifyNoInteractions(pdfGeneratorGateway);
    }

}
