package br.com.pj2.back.core.gateway;

import java.io.File;

public interface PdfGeneratorGateway {

    File generatePdf(String registration, Integer month, Integer year);

}
