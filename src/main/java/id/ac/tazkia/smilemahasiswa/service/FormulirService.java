package id.ac.tazkia.smilemahasiswa.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Seminar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FormulirService {

    @Value("${logo.surat}")
    private String logo;

    @Autowired
    private KrsDetailDao krsDetailDao;

    public void formulirSidangReguler(@RequestParam Seminar seminar, HttpServletResponse response) throws IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(seminar.getNote().getMahasiswa().getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(seminar.getNote().getMahasiswa().getId());

        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

        document.open();

        Image lTazkia = Image.getInstance(logo + File.separator + "logo-formulir.jpg");
        lTazkia.scaleToFit(450, 450);
        lTazkia.setAbsolutePosition(90, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 5);
        document.add(lTazkia);

        Paragraph newLine = new Paragraph("\n\n");
        document.add(newLine);
        document.add(newLine);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(90);
        table.setWidths(new int[] {3,1,8,5,1,8});

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("No. Dok"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("TAZ/FM/KPS/10.04"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Tgl. Efektif"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Revisi"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("1"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Halaman"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("1 dari 1"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        document.add(table);
        document.add(newLine);

        PdfPTable tableJudul = new PdfPTable(3);
        tableJudul.setWidthPercentage(90);
        tableJudul.setWidths(new int[] {3,1,10});

        cell = new PdfPCell(new Phrase("Judul", new Font(Font.BOLD, 20)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableJudul.addCell(cell);
        cell = new PdfPCell(new Phrase(":", new Font(Font.BOLD, 20)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableJudul.addCell(cell);
        cell = new PdfPCell(new Phrase("Pendaftaran Sidang Skripsi", new Font(Font.BOLD, 20)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableJudul.addCell(cell);
        document.add(tableJudul);
        document.add(newLine);

        Paragraph paragrafkosong;
        paragrafkosong = new Paragraph("Sesuai dengan ketentuan yang berlaku dalam prosedur sidang skripsi, maka dengan ini saya yang bertanda tangan di bawah ini :");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);

        PdfPTable tableMhs = new PdfPTable(3);
        tableMhs.setWidthPercentage(90);
        tableMhs.setWidths(new int[] {10,1,15});
        tableMhs.setSpacingBefore(10);

        cell = new PdfPCell(new Phrase("Nama"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getNama()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("NIM"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getNim()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("Program Studi"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getIdProdi().getNamaProdi()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("SKS yang telah diselesaikan"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(totalSKS.toString()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("IPK"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(ipk.toString()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("Judul Skripsi"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("................................................................................................................................................................................................"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        document.add(tableMhs);

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Sitasi minimal 2 untuk S1 dan 4 untuk S2 (Judul - Nama - Peneliti - Buku/Jurnal):"));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("1. "));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("2. "));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("3. "));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("4. "));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);

        paragrafkosong = new Paragraph("Dengan ini mengajukan permohonan untuk dilakukan sidang skripsi.");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);
        paragrafkosong = new Paragraph("Yang mengajukan, ");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);
        document.add(new Paragraph("\n"));

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Bogor, ........................."));
        paragrafkosong.add(new Chunk("\n\n\n\n"));
        paragrafkosong.add(new Chunk("...................................."));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("------------------------------"));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);

        paragrafkosong = new Paragraph("Disetujui oleh, ");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(220);
        document.add(paragrafkosong);

        PdfPTable tableTTD = new PdfPTable(3);
        tableTTD.setWidths(new int[] {7,10,7});
        tableTTD.setSpacingBefore(10);

        cell = new PdfPCell(new Phrase("Dosen Pembimbing I"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableTTD.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableTTD.addCell(cell);
        cell = new PdfPCell(new Phrase("Dosen Pembimbing II"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tableTTD.addCell(cell);
        document.add(tableTTD);
        document.add(newLine);
        document.add(newLine);

        PdfPTable tableDot = new PdfPTable(3);
        tableDot.setWidths(new int[] {7,10,7});
        tableDot.setSpacingBefore(10);

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk(".................................."));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("----------------------------"));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);

        cell = new PdfPCell(new Phrase(paragrafkosong));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableDot.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableDot.addCell(cell);
        cell = new PdfPCell(new Phrase(paragrafkosong));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tableDot.addCell(cell);
        document.add(tableDot);

        document.close();

    }

    public void formulirSidangPasca(@RequestParam Seminar seminar, HttpServletResponse response) throws IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(seminar.getNote().getMahasiswa().getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(seminar.getNote().getMahasiswa().getId());

        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

        document.open();

        Image lTazkia = Image.getInstance(logo + File.separator + "logo-formulir.jpg");
        lTazkia.scaleToFit(450, 450);
        lTazkia.setAbsolutePosition(90, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 5);
        document.add(lTazkia);

        Paragraph newLine = new Paragraph("\n\n");
        document.add(newLine);
        document.add(newLine);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(90);
        table.setWidths(new int[] {3,1,8,5,1,8});

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("No. Dok"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("TAZ/FM/KPS/10.04"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Tgl. Efektif"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Revisi"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("1"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Halaman"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("1 dari 1"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        document.add(table);
        document.add(newLine);

        PdfPTable tableJudul = new PdfPTable(3);
        tableJudul.setWidthPercentage(90);
        tableJudul.setWidths(new int[] {3,1,10});

        cell = new PdfPCell(new Phrase("Judul", new Font(Font.BOLD, 20)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableJudul.addCell(cell);
        cell = new PdfPCell(new Phrase(":", new Font(Font.BOLD, 20)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableJudul.addCell(cell);
        cell = new PdfPCell(new Phrase("Pendaftaran Sidang Tesis", new Font(Font.BOLD, 20)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableJudul.addCell(cell);
        document.add(tableJudul);
        document.add(newLine);

        Paragraph paragrafkosong;
        paragrafkosong = new Paragraph("Sesuai dengan ketentuan yang berlaku dalam prosedur sidang Tesis, maka dengan ini saya yang bertanda tangan di bawah ini :");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);

        PdfPTable tableMhs = new PdfPTable(3);
        tableMhs.setWidthPercentage(90);
        tableMhs.setWidths(new int[] {10,1,15});
        tableMhs.setSpacingBefore(10);

        cell = new PdfPCell(new Phrase("Nama"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getNama()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("NIM"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getNim()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("Program Studi"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getIdProdi().getNamaProdi()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("SKS yang telah diselesaikan"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(totalSKS.toString()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("IPK"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(ipk.toString()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("Judul Tesis"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_LEFT);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase(":"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        cell = new PdfPCell(new Phrase("................................................................................................................................................................................................"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableMhs.addCell(cell);
        document.add(tableMhs);

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Sitasi minimal 2 untuk S1 dan 4 untuk S2 (Judul - Nama - Peneliti - Buku/Jurnal):"));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("1. "));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("2. "));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("3. "));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("4. "));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);

        paragrafkosong = new Paragraph("Dengan ini mengajukan permohonan untuk dilakukan sidang Tesis.");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);
        paragrafkosong = new Paragraph("Yang mengajukan, ");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);
        document.add(new Paragraph("\n"));

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Bogor, ........................."));
        paragrafkosong.add(new Chunk("\n\n\n\n"));
        paragrafkosong.add(new Chunk("...................................."));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("------------------------------"));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(28);
        document.add(paragrafkosong);

        paragrafkosong = new Paragraph("Disetujui oleh, ");
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(220);
        document.add(paragrafkosong);

        PdfPTable tableTTD = new PdfPTable(3);
        tableTTD.setWidths(new int[] {7,10,7});
        tableTTD.setSpacingBefore(10);

        cell = new PdfPCell(new Phrase("Dosen Pembimbing I"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableTTD.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableTTD.addCell(cell);
        cell = new PdfPCell(new Phrase("Dosen Pembimbing II"));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tableTTD.addCell(cell);
        document.add(tableTTD);
        document.add(newLine);
        document.add(newLine);

        PdfPTable tableDot = new PdfPTable(3);
        tableDot.setWidths(new int[] {7,10,7});
        tableDot.setSpacingBefore(10);

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk(".................................."));
        paragrafkosong.add(new Chunk("\n"));
        paragrafkosong.add(new Chunk("----------------------------"));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);

        cell = new PdfPCell(new Phrase(paragrafkosong));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableDot.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableDot.addCell(cell);
        cell = new PdfPCell(new Phrase(paragrafkosong));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tableDot.addCell(cell);
        document.add(tableDot);

        document.close();

    }

}
