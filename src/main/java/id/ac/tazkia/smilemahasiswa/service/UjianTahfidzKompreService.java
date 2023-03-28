package id.ac.tazkia.smilemahasiswa.service;


import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class UjianTahfidzKompreService {

    @Value("${logo.surat}")
    private String logo;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private GmailApiService gmailApiService;

    @Autowired
    private MustacheFactory mustacheFactory;

    public void formulirTahfidz(Mahasiswa mahasiswa, HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());

        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

        document.open();

        Image background = Image.getInstance(logo + File.separator + "LOGO TR.png");
        background.setAbsolutePosition(400, 190);
        background.scaleAbsolute(400, 450);
        document.add(background);

        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(100, 100);
        lTazkia.setAbsolutePosition(20, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 15);
        Paragraph newLine = new Paragraph("Formulir");
        newLine.setAlignment(Element.ALIGN_RIGHT);
        Paragraph nLine = new Paragraph("\n");
        document.add(lTazkia);

        document.add(nLine);
        document.add(newLine);


        document.add(nLine);
        document.add(nLine);
        document.add(nLine);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new int[] {2,1,4,2,1,4});
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("No. Dok",font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("TAZ/FM/KPS/10.01", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Tgl. Efektif",font));
        cell.setBorderColor(Color.GRAY);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":"));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Revisi",font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":"));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("1", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Halaman",font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":", font));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("1 dari 1", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        document.add(table);

        PdfPTable judul = new PdfPTable(1);
        judul.setWidthPercentage(100);
        judul.setWidths(new int[] {10});
        Font fontJudul = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14);
        fontJudul.setColor(CMYKColor.BLACK);
        fontJudul.setStyle(Font.BOLD);

        cell = new PdfPCell(new Phrase("Judul : Pendaftaran Sidang Ujian Tahfidz", fontJudul));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingLeft(10);
        cell.setPaddingTop(5);
        cell.setPaddingRight(10);
        cell.setPaddingBottom(5);
        judul.addCell(cell);

        document.add(nLine);
        document.add(judul);

        document.add(nLine);
        Paragraph keterangan = new Paragraph("Sesuai dengan ketentuan yang berlaku dalam prosedur Sidang Ujian Tahfidz, " +
                "maka dengan ini saya yang bertanda tangan di bawah ini :", font);
        document.add(keterangan);
        document.add(nLine);

        PdfPTable formFormulir = new PdfPTable(3);
        formFormulir.setWidthPercentage(90);
        formFormulir.setWidths(new int[] {2,1,7});

        cell = new PdfPCell(new Phrase("Nama Mahasiswa",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(mahasiswa.getNama(),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase("NIM",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(mahasiswa.getNim(),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);


        cell = new PdfPCell(new Phrase("Program Studi",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(mahasiswa.getIdProdi().getNamaProdi(),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase("IPK",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(String.valueOf(ipk),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase("No. Hand Phone",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(mahasiswa.getTeleponSeluler(),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);


        document.add(formFormulir);
        document.add(nLine);

        Paragraph keteranganKet = new Paragraph("Menyatakan telah lulus semua mata kuliah dan dengan ini " +
                "mengajukan pendaftaran untuk :", font);

        document.add(keteranganKet);
        document.add(nLine);

        Paragraph jenisUjian = new Paragraph("1. Ujian Tahfidz", fontJudul);
        jenisUjian.setAlignment(Element.ALIGN_CENTER);

        document.add(jenisUjian);
        document.add(nLine);
        document.add(nLine);

        Paragraph yang = new Paragraph("Yang Mengajukan,", font);
        Paragraph tgl = new Paragraph("Bogor,..............................", font);
        Paragraph titik = new Paragraph("....................................................", font);
        document.add(yang);
        document.add(tgl);

        Image logoTdd = Image.getInstance(logo + File.separator + "LOGO.png");
        logoTdd.scaleToFit(80, 70);
        logoTdd.setAbsolutePosition(95, PageSize.A4.getHeight() - (logoTdd.getScaledHeight()) - 502);
        document.add(logoTdd);
        document.add(nLine);
        document.add(nLine);
        document.add(nLine);

        document.add(titik);

        Image footer = Image.getInstance(logo + File.separator + "footer.png");
        footer.scaleToFit(document.getPageSize().getWidth() - 20, 200);
        footer.setAbsolutePosition( 10, 10);
        document.add(footer);




        document.close();
    }

    public void formulirKompre(Mahasiswa mahasiswa, HttpServletResponse response) throws IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());

        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

        document.open();

        Image background = Image.getInstance(logo + File.separator + "LOGO TR.png");
        background.setAbsolutePosition(400, 190);
        background.scaleAbsolute(400, 450);
        document.add(background);

        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(100, 100);
        lTazkia.setAbsolutePosition(20, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 15);
        Paragraph newLine = new Paragraph("Formulir");
        newLine.setAlignment(Element.ALIGN_RIGHT);
        Paragraph nLine = new Paragraph("\n");
        document.add(lTazkia);

        document.add(nLine);
        document.add(newLine);


        document.add(nLine);
        document.add(nLine);
        document.add(nLine);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new int[] {2,1,4,2,1,4});
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("No. Dok",font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("TAZ/FM/KPS/10.04", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Tgl. Efektif",font));
        cell.setBorderColor(Color.GRAY);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":"));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Revisi",font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":"));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("1", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Halaman",font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(":", font));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("1 dari 1", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.GRAY);
        cell.setPadding(5);
        table.addCell(cell);

        document.add(table);

        PdfPTable judul = new PdfPTable(1);
        judul.setWidthPercentage(100);
        judul.setWidths(new int[] {10});
        Font fontJudul = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14);
        fontJudul.setColor(CMYKColor.BLACK);
        fontJudul.setStyle(Font.BOLD);

        cell = new PdfPCell(new Phrase("Judul   : Pendaftaran Sidang Komprehensif", fontJudul));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingLeft(10);
        cell.setPaddingTop(5);
        cell.setPaddingRight(10);
        cell.setPaddingBottom(5);
        judul.addCell(cell);

        document.add(nLine);
        document.add(judul);

        document.add(nLine);
        Paragraph keterangan = new Paragraph("Sesuai dengan ketentuan yang berlaku dalam prosedur Sidang Komprehensif, " +
                "maka dengan ini saya yang bertanda tangan di bawah ini :", font);
        document.add(keterangan);
        document.add(nLine);

        PdfPTable formFormulir = new PdfPTable(3);
        formFormulir.setWidthPercentage(90);
        formFormulir.setWidths(new int[] {2,1,7});

        cell = new PdfPCell(new Phrase("Nama Mahasiswa",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(mahasiswa.getNama(),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase("NIM",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(mahasiswa.getNim(),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);


        cell = new PdfPCell(new Phrase("Program Studi",font))
;
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(mahasiswa.getIdProdi().getNamaProdi(),font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);


        cell = new PdfPCell(new Phrase("Konsentrasi",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(".............................",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);

//        if (mahasiswa.getIdKonsentrasi() == null){
//
//        }else {
//            cell = new PdfPCell(new Phrase(mahasiswa.getIdKonsentrasi().getNamaKonsentrasi(),font));
//            cell.setBorder(Rectangle.NO_BORDER);
//            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//            formFormulir.addCell(cell);
//        }


        cell = new PdfPCell(new Phrase("IPK/SKS",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(":",font));
        cell.setBorder(Rectangle.NO_BORDER);
        formFormulir.addCell(cell);

        cell = new PdfPCell(new Phrase(String.valueOf(ipk) + "/" + totalSKS,font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formFormulir.addCell(cell);



        document.add(formFormulir);
        document.add(nLine);

        Paragraph keteranganKet = new Paragraph("Menyatakan telah lulus semua mata kuliah dan dengan ini " +
                "mengajukan pendaftaran untuk :", font);

        document.add(keteranganKet);
        document.add(nLine);

        Paragraph jenisUjian = new Paragraph("1. Sidang Komprehensif", fontJudul);
        jenisUjian.setAlignment(Element.ALIGN_CENTER);

        document.add(jenisUjian);
        document.add(nLine);
        document.add(nLine);

        Paragraph yang = new Paragraph("Yang Mengajukan,", font);
        Paragraph tgl = new Paragraph("Bogor,..............................", font);
        Paragraph titik = new Paragraph("....................................................", font);
        document.add(yang);
        document.add(tgl);

        Image logoTdd = Image.getInstance(logo + File.separator + "LOGO.png");
        logoTdd.scaleToFit(80, 70);
        logoTdd.setAbsolutePosition(95, PageSize.A4.getHeight() - (logoTdd.getScaledHeight()) - 502);
        document.add(logoTdd);
        document.add(nLine);
        document.add(nLine);
        document.add(nLine);

        document.add(titik);

        Image footer = Image.getInstance(logo + File.separator + "footer.png");
        footer.scaleToFit(document.getPageSize().getWidth() - 20, 200);
        footer.setAbsolutePosition( 10, 10);
        document.add(footer);




        document.close();

    }

    public void notifEmail(String email, String jenisUjian, String status){
        Mustache templateEmail = mustacheFactory.compile("templates/ujian/notif/email.html");
        Map<String, String> data = new HashMap<>();
        if (jenisUjian.equals("UjianTahfidz") && status.equals("APPROVED")){
            data.put("notif", "Selamat Pendaftaran Ujian Tahfidz Anda Berhasil");
        } else if (jenisUjian.equals("UjianKomprehensif") && status.equals("APPROVED")) {
            data.put("notif", "Selamat Pendaftaran Ujian Komprehensif Anda Berhasil");
        } else if (jenisUjian.equals("UjianTahfidz") && status.equals("REJECTED")) {
            data.put("notif", "Pendaftaran Ujian Tahfidz anda ditolak.\n" +
                    "Silakan cek smile dan hubungi bagian Akademik.");
        } else if (jenisUjian.equals("UjianKomprehensif") && status.equals("REJECTED")) {
            data.put("notif", "Pendaftaran Ujian Komprehensif anda ditolak.\n" +
                    "Silakan cek smile dan hubungi bagian Akademik.");
        }


        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);
        gmailApiService.kirimEmail(
                jenisUjian,
                email,
                "Nofitifikasi" + jenisUjian,
                output.toString()
        );

    }
}
