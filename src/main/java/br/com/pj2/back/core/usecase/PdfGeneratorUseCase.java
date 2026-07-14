package br.com.pj2.back.core.usecase;

import br.com.pj2.back.core.gateway.PdfGeneratorGateway;
import br.com.pj2.back.core.gateway.TokenGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class PdfGeneratorUseCase {

    private final TokenGateway tokenGateway;
    private final PdfGeneratorGateway pdfGeneratorGateway;

    public File execute(String authorizationHeader, Integer month, Integer year) {
        String registration = tokenGateway.extractSubjectFromAuthorization(authorizationHeader);
        return pdfGeneratorGateway.generatePdf(registration, month, year);
    }
}
