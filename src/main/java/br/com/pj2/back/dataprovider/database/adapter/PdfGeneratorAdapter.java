package br.com.pj2.back.dataprovider.database.adapter;

import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.BadRequestException;
import br.com.pj2.back.core.gateway.PdfGeneratorGateway;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PdfGeneratorAdapter implements PdfGeneratorGateway {

    private final MonitoringSessionAdapter monitoringSessionAdapter;

    private final static int HORAS_MENSAIS = 40;


    @Override
    public File generatePdf(String registration, Integer month, Integer year) {
        try {
            Duration workedHoursByMonth = monitoringSessionAdapter.getWorkedHoursByMonth(registration, month, year);
            long workedHours = workedHoursByMonth.toHours();

            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            File pdfFile = File.createTempFile("declaracao_monitoria", ".pdf");
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
            doc.open();

            Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Paragraph paragraph = new Paragraph();
            paragraph.setFont(font);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Paragraph title = new Paragraph("Declaração de Cumprimento de Carga Horária", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            doc.add(title);

            Paragraph msg = new Paragraph("Programa de Monitoria Coord. Do CST em Análise e Desenvolvimento de Sistemas", new Font(FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK)));
            msg.setAlignment(Element.ALIGN_CENTER);
            doc.add(msg);
            doc.add(Chunk.NEWLINE);

            paragraph.add("Eu, _________________________________________________ (SIAPE: _____________),\n");
            paragraph.add("professor responsável pela disciplina __________________________________________ do\n");
            paragraph.add("Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas (CST em ADS),\n");
            paragraph.add("orientador do monitor __________________________________________________, atesto\n");

            if (workedHours == 0) {
                paragraph.add("que o mesmo, durante o mês de _______________________ do ano de __________, cumpriu,\n");
            } else {
                paragraph.add("que o mesmo, durante o mês de _______________________ do ano de __________, não cumpriu,\n");
            }

            paragraph.add("semanalmente, 10 horas de atividades de apoio ao ensino, conforme previsto no Edital nº\n");
            paragraph.add("________, publicado em ________ de ________ de ________, item ____.\n\n");

            paragraph.add(String.format("Horas totais cumpridas no mês: %dh %02dmin%n%n",
                    workedHoursByMonth.toHours(),
                    workedHoursByMonth.toMinutesPart()
            ));
            doc.add(paragraph);

            if (workedHours < 40) {
                Paragraph justificativa = new Paragraph("Justificativa (caso não tenha cumprido as 40 horas mensais):\n\n", font);
                doc.add(justificativa);

                PdfContentByte canvas = writer.getDirectContent();
                float left = doc.leftMargin();
                float right = PageSize.A4.getWidth() - doc.rightMargin();
                float bottomY = 460f;
                float height = 80f;

                canvas.rectangle(left, bottomY, right - left, height);
                canvas.stroke();

                float lineY = bottomY + height - 20f;
                for (int i = 0; i < 3; i++) {
                    canvas.moveTo(left + 5, lineY);
                    canvas.lineTo(right - 5, lineY);
                    lineY -= 20f;
                }
                canvas.stroke();
                doc.add(Chunk.NEWLINE);
                doc.add(Chunk.NEWLINE);
                doc.add(Chunk.NEWLINE);
                doc.add(Chunk.NEWLINE);
                doc.add(Chunk.NEWLINE);
            }

            PdfPTable assinaturaTable = new PdfPTable(2);
            assinaturaTable.setWidthPercentage(100);
            assinaturaTable.setWidths(new int[]{1, 1});

            PdfPCell profCell = new PdfPCell();
            profCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            profCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            profCell.addElement(new Paragraph("___________________________________", font));
            profCell.addElement(new Paragraph("Assinatura do Docente", font));
            profCell.addElement(new Paragraph("SIAPE: _____________________________", font));

            PdfPCell alunoCell = new PdfPCell();
            alunoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            alunoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            alunoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            alunoCell.addElement(new Paragraph("___________________________________", font));
            alunoCell.addElement(new Paragraph("Assinatura do Aluno", font));
            alunoCell.addElement(new Paragraph("Matrícula: ___________________________", font));

            assinaturaTable.addCell(profCell);
            assinaturaTable.addCell(alunoCell);

            doc.add(Chunk.NEWLINE);
            doc.add(assinaturaTable);

            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);

            Paragraph coordAssinatura = new Paragraph();
            coordAssinatura.setFont(font);
            coordAssinatura.setAlignment(Element.ALIGN_CENTER);
            coordAssinatura.add("___________________________________\n");
            coordAssinatura.add("Assinatura e Carimbo do Coordenador\n");

            doc.add(coordAssinatura);

            doc.close();
            return pdfFile;

        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.SERVER_ERROR);
        }
    }

}
