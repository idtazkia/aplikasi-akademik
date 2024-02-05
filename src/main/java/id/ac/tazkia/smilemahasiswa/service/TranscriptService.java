package id.ac.tazkia.smilemahasiswa.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MatakuliahDao;
import id.ac.tazkia.smilemahasiswa.dao.TranskriptDao;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Matakuliah;
import id.ac.tazkia.smilemahasiswa.entity.Transkript;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@Slf4j
public class TranscriptService {

    @Value("${logo.surat}")
    private String logo;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private MatakuliahDao matakuliahDao;

    @Autowired
    private TranskriptDao transkriptDao;

    // transkrip sementara
    public void transkripSementara(@RequestParam Mahasiswa mhs, @RequestParam(required = false) List<Object[]> semester1, @RequestParam(required = false)List<Object[]> semester2,
                                   @RequestParam(required = false)List<Object[]> semester3, @RequestParam(required = false)List<Object[]> semester4,
                                   @RequestParam(required = false)List<Object[]> semester5, @RequestParam(required = false)List<Object[]> semester6,
                                   @RequestParam(required = false)List<Object[]> semester7, @RequestParam(required = false)List<Object[]> semester8,
                                   @RequestParam(required = false) Long sks, @RequestParam(required = false) IpkDto ipk, @RequestParam(required = false) Long mutu,
                                   HttpServletResponse response) throws IOException{

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font tTrans = FontFactory.getFont(FontFactory.HELVETICA, 8);

        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(60, 60);
        lTazkia.setAbsolutePosition(80, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 45);

        document.add(lTazkia);

        Paragraph tazkia = new Paragraph("INSTITUT AGAMA ISLAM TAZKIA", titleFont);
        tazkia.setAlignment(Element.ALIGN_CENTER);
        Paragraph nLine = new Paragraph("\n");

        document.add(tazkia);

        Paragraph tTranskrip = new Paragraph("Transkrip Nilai Akademik", tTrans);
        tTranskrip.setAlignment(Element.ALIGN_CENTER);
        document.add(tTranskrip);
        document.add(nLine);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(50);
        table.setWidths(new int[] {3, 1, 5});
        Font font = FontFactory.getFont(FontFactory.defaultEncoding, 8);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("NIM",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getNim(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Nama",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getNama(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Program Studi",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getIdProdi().getNamaProdi(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        document.add(table);

        PdfPTable tabNilai = new PdfPTable(7);
        tabNilai.setWidthPercentage(100);
        tabNilai.setWidths(new int[] {3,4,7,1,3,2,3});
        tabNilai.setSpacingBefore(10);
        Font fNilai = FontFactory.getFont(FontFactory.defaultEncoding, 8);
        font.setColor(CMYKColor.BLACK);
        Font fHeader = FontFactory.getFont(FontFactory.defaultEncoding, 8, Font.BOLD);
        fHeader.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Semester", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Kode MK", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Nama Mata Kuliah", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("SKS", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Nilai", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Bobot", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Mutu", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        if (!semester1.isEmpty()){
            cell = new PdfPCell(new Phrase("1", fNilai));
            cell.setRowspan(semester1.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester1){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester2.isEmpty()) {
            cell = new PdfPCell(new Phrase("2", fNilai));
            cell.setRowspan(semester2.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester2){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester3.isEmpty()) {
            cell = new PdfPCell(new Phrase("3", fNilai));
            cell.setRowspan(semester3.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester3){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester4.isEmpty()) {
            cell = new PdfPCell(new Phrase("4", fNilai));
            cell.setRowspan(semester4.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester4){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester5.isEmpty()) {
            cell = new PdfPCell(new Phrase("5", fNilai));
            cell.setRowspan(semester5.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester5){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester6.isEmpty()) {
            cell = new PdfPCell(new Phrase("6", fNilai));
            cell.setRowspan(semester6.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester6){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester7.isEmpty()) {
            cell = new PdfPCell(new Phrase("7", fNilai));
            cell.setRowspan(semester7.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester7){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester8.isEmpty()) {
            System.out.println("cek semester 8 : " + semester8);
            cell = new PdfPCell(new Phrase("8", fNilai));
            cell.setRowspan(semester8.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (Object[] data : semester8){
                cell = new PdfPCell(new Phrase(data[1].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[2].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[3].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[4].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[5].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data[6].toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        cell = new PdfPCell(new Phrase("Jumlah : ", fNilai));
        cell.setColspan(3);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(sks), fNilai));
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setColspan(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(mutu), fNilai));
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Indeks Prestasi Kumulatif (IPK) : ", fNilai));
        cell.setColspan(3);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(ipk.getIpk().toString(), fNilai));
        cell.setColspan(4);
        tabNilai.addCell(cell);

        document.add(tabNilai);

        document.add(nLine);

        Paragraph tgl = new Paragraph("Bogor, " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD));
        document.add(tgl);

        Paragraph title = new Paragraph(mhs.getIdProdi().getJabatan(), fNilai);
        document.add(title);

        document.add(nLine);
        document.add(nLine);

        Paragraph kpsName = new Paragraph(mhs.getIdProdi().getDosen().getKaryawan().getNamaKaryawan(), fNilai);
        document.add(kpsName);

        document.close();

    }

    // transkrip bahasa indonesia
    public void transkripIndo(@RequestParam Mahasiswa mhs, @RequestParam(required = false) List<DataTranskript> semester1, @RequestParam(required = false)List<DataTranskript> semester2,
                              @RequestParam(required = false)List<DataTranskript> semester3, @RequestParam(required = false)List<DataTranskript> semester4,
                              @RequestParam(required = false)List<DataTranskript> semester5, @RequestParam(required = false)List<DataTranskript> semester6,
                              @RequestParam(required = false)List<DataTranskript> semester7, @RequestParam(required = false)List<DataTranskript> semester8,
                              @RequestParam(required = false) int sks, @RequestParam(required = false) BigDecimal ipk, @RequestParam(required = false) BigDecimal mutu,
                              HttpServletResponse response) throws IOException {


        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font tTrans = FontFactory.getFont(FontFactory.HELVETICA, 8);

        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(60, 60);
        lTazkia.setAbsolutePosition(80, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 45);

        document.add(lTazkia);

        Paragraph tazkia = new Paragraph("INSTITUT AGAMA ISLAM TAZKIA", titleFont);
        tazkia.setAlignment(Element.ALIGN_CENTER);
        Paragraph nLine = new Paragraph("\n");

        document.add(tazkia);

        Paragraph tTranskrip = new Paragraph("Transkrip Nilai Akademik", tTrans);
        tTranskrip.setAlignment(Element.ALIGN_CENTER);
        document.add(tTranskrip);
        document.add(nLine);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(50);
        table.setWidths(new int[] {3, 1, 5});
        Font font = FontFactory.getFont(FontFactory.defaultEncoding, 8);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("NIM",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getNim(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Nama",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getNama(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Program Studi",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getIdProdi().getNamaProdi(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        document.add(table);

        PdfPTable tabNilai = new PdfPTable(7);
        tabNilai.setWidthPercentage(100);
        tabNilai.setWidths(new int[] {3,4,7,1,3,2,3});
        tabNilai.setSpacingBefore(10);
        Font fNilai = FontFactory.getFont(FontFactory.defaultEncoding, 8);
        font.setColor(CMYKColor.BLACK);
        Font fHeader = FontFactory.getFont(FontFactory.defaultEncoding, 8, Font.BOLD);
        fHeader.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Semester", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Kode MK", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Nama Mata Kuliah", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("SKS", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Nilai", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Bobot", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Mutu", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        if (!semester1.isEmpty()){
            cell = new PdfPCell(new Phrase("1", fNilai));
            cell.setRowspan(semester1.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester1){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester2.isEmpty()) {
            cell = new PdfPCell(new Phrase("2", fNilai));
            cell.setRowspan(semester2.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester2){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester3.isEmpty()) {
            cell = new PdfPCell(new Phrase("3", fNilai));
            cell.setRowspan(semester3.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester3){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester4.isEmpty()) {
            cell = new PdfPCell(new Phrase("4", fNilai));
            cell.setRowspan(semester4.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester4){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester5.isEmpty()) {
            cell = new PdfPCell(new Phrase("5", fNilai));
            cell.setRowspan(semester5.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester5){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester6.isEmpty()) {
            cell = new PdfPCell(new Phrase("6", fNilai));
            cell.setRowspan(semester6.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester6){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester7.isEmpty()) {
            cell = new PdfPCell(new Phrase("7", fNilai));
            cell.setRowspan(semester7.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester7){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester8.isEmpty()) {
            System.out.println("cek semester 8 : " + semester8);
            cell = new PdfPCell(new Phrase("8", fNilai));
            cell.setRowspan(semester8.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester8){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMatkul(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        cell = new PdfPCell(new Phrase("Jumlah : ", fNilai));
        cell.setColspan(3);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(sks), fNilai));
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setColspan(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(mutu), fNilai));
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Indeks Prestasi Kumulatif (IPK) : ", fNilai));
        cell.setColspan(3);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(ipk), fNilai));
        cell.setColspan(4);
        tabNilai.addCell(cell);

        document.add(tabNilai);

        document.add(nLine);

        Paragraph tgl = new Paragraph("Bogor, " + convertDateIndonesia(LocalDate.now()), FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD));
        document.add(tgl);

        Paragraph title = new Paragraph(mhs.getIdProdi().getJabatan(), fNilai);
        document.add(title);

        document.add(nLine);
        document.add(nLine);

        Paragraph kpsName = new Paragraph(mhs.getIdProdi().getDosen().getKaryawan().getNamaKaryawan(), fNilai);
        document.add(kpsName);

        document.close();

    }

    // transkrip bahasa inggris
    public void transcriptEnglish(Mahasiswa mhs, @RequestParam(required = false) List<DataTranskript> semester1, @RequestParam(required = false)List<DataTranskript> semester2,
                                  @RequestParam(required = false)List<DataTranskript> semester3, @RequestParam(required = false)List<DataTranskript> semester4,
                                  @RequestParam(required = false)List<DataTranskript> semester5, @RequestParam(required = false)List<DataTranskript> semester6,
                                  @RequestParam(required = false)List<DataTranskript> semester7, @RequestParam(required = false)List<DataTranskript> semester8,
                                  @RequestParam(required = false) int sks, @RequestParam(required = false) BigDecimal ipk, @RequestParam(required = false) BigDecimal mutu,
                                  HttpServletResponse response) throws Exception{

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font tTrans = FontFactory.getFont(FontFactory.HELVETICA, 8);

        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(60, 60);
        lTazkia.setAbsolutePosition(80, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 45);

        document.add(lTazkia);

        Paragraph tazkia = new Paragraph("INSTITUT AGAMA ISLAM TAZKIA", titleFont);
        tazkia.setAlignment(Element.ALIGN_CENTER);
        Paragraph nLine = new Paragraph("\n");

        document.add(tazkia);

        Paragraph tTranskrip = new Paragraph("Academic Transcripts", tTrans);
        tTranskrip.setAlignment(Element.ALIGN_CENTER);
        document.add(tTranskrip);
        document.add(nLine);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(50);
        table.setWidths(new int[] {3, 1, 5});
        Font font = FontFactory.getFont(FontFactory.defaultEncoding, 8);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("NIM",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getNim(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Name",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getNama(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Department",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(mhs.getIdProdi().getNamaProdiEnglish(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        document.add(table);

        PdfPTable tabNilai = new PdfPTable(7);
        tabNilai.setWidthPercentage(100);
        tabNilai.setWidths(new int[] {3,4,7,1,3,2,3});
        tabNilai.setSpacingBefore(10);
        Font fNilai = FontFactory.getFont(FontFactory.defaultEncoding, 8);
        font.setColor(CMYKColor.BLACK);
        Font fHeader = FontFactory.getFont(FontFactory.defaultEncoding, 8, Font.BOLD);
        fHeader.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Semester", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Course Code", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Course Title", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("SKS", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Grade Point", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Grade", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Total Mark", fHeader));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        tabNilai.addCell(cell);
        if (!semester1.isEmpty()){
            cell = new PdfPCell(new Phrase("1", fNilai));
            cell.setRowspan(semester1.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester1){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester2.isEmpty()) {
            cell = new PdfPCell(new Phrase("2", fNilai));
            cell.setRowspan(semester2.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester2){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester3.isEmpty()) {
            cell = new PdfPCell(new Phrase("3", fNilai));
            cell.setRowspan(semester3.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester3){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester4.isEmpty()) {
            cell = new PdfPCell(new Phrase("4", fNilai));
            cell.setRowspan(semester4.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester4){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester5.isEmpty()) {
            cell = new PdfPCell(new Phrase("5", fNilai));
            cell.setRowspan(semester5.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester5){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester6.isEmpty()) {
            cell = new PdfPCell(new Phrase("6", fNilai));
            cell.setRowspan(semester6.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester6){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester7.isEmpty()) {
            cell = new PdfPCell(new Phrase("7", fNilai));
            cell.setRowspan(semester7.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester7){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        if (!semester8.isEmpty()) {
            System.out.println("cek semester 8 : " + semester8);
            cell = new PdfPCell(new Phrase("8", fNilai));
            cell.setRowspan(semester8.size());
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            tabNilai.addCell(cell);
            for (DataTranskript data : semester8){
                cell = new PdfPCell(new Phrase(data.getKode(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getCourse(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getSks().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getGrade(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getBobot().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
                cell = new PdfPCell(new Phrase(data.getMutu().toString(), fNilai));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabNilai.addCell(cell);
            }
        }

        cell = new PdfPCell(new Phrase("Jumlah : ", fNilai));
        cell.setColspan(3);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(sks), fNilai));
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setColspan(2);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(mutu), fNilai));
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Cumulative Grade Point Average : ", fNilai));
        cell.setColspan(3);
        tabNilai.addCell(cell);
        cell = new PdfPCell(new Phrase(String.valueOf(ipk), fNilai));
        cell.setColspan(4);
        tabNilai.addCell(cell);

        document.add(tabNilai);

        document.add(nLine);

        Paragraph tgl = new Paragraph("Bogor, " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD));
        document.add(tgl);

        Paragraph title = new Paragraph(mhs.getIdProdi().getNamaProdiEnglish() + " Coordinator", fNilai);
        document.add(title);

        document.add(nLine);
        document.add(nLine);

        Paragraph kpsName = new Paragraph(mhs.getIdProdi().getDosen().getKaryawan().getNamaKaryawan(), fNilai);
        document.add(kpsName);

        document.close();


    }

    public String convertDateIndonesia(LocalDate date){

        String result = null;

        switch (date.getMonthValue()){
            case 1:
                result = date.getDayOfMonth() + " Januari " + date.getYear();
                break;
            case 2:
                result = date.getDayOfMonth() + " Februari " + date.getYear();
                break;
            case 3:
                result = date.getDayOfMonth() + " Maret " + date.getYear();
                break;
            case 4:
                result = date.getDayOfMonth() + " April " + date.getYear();
                break;
            case 5:
                result = date.getDayOfMonth() + " Mei " + date.getYear();
                break;
            case 6:
                result = date.getDayOfMonth() + " Juni " + date.getYear();
                break;
            case 7:
                result = date.getDayOfMonth() + " Juli " + date.getYear();
                break;
            case 8:
                result = date.getDayOfMonth() + " Agustus " + date.getYear();
                break;
            case 9:
                result = date.getDayOfMonth() + " September " + date.getYear();
                break;
            case 10:
                result = date.getDayOfMonth() + " Oktober " + date.getYear();
                break;
            case 11:
                result = date.getDayOfMonth() + " November " + date.getYear();
                break;
            case 12:
                result = date.getDayOfMonth() + " Desember " + date.getYear();
                break;
            default:
                result = null;
                break;
        }

        return result;

    }

    public BaseResponse inputTrancript(List<Mahasiswa> mahasiswas){
        BaseResponse response = null;

        for (Mahasiswa mhs : mahasiswas){
            List<DataTranskript> listTranscript = krsDetailDao.listTranskript(mhs);

            for (DataTranskript data : listTranscript){
                log.info("Input data transkript mahasiswa {} - {} sem {} matkul {} grade {}", mhs.getNim(), mhs.getNama(), data.getSemester(), data.getMatkul(), data.getGrade());
                saveTranskript(data, mhs);
            }
        }

        response = BaseResponse.builder()
                .code("200").message("Berhasil Import")
                .build();

        return response;
    }

    public void saveTranskript(DataTranskript data, Mahasiswa mahasiswa){

        Matakuliah m = matakuliahDao.findById(data.getIdmatakuliah()).get();
        Transkript cekData = transkriptDao.findByMahasiswaAndMatakuliahAndSemester(mahasiswa, m, data.getSemester());
        if (cekData == null) {
            Transkript transkript = new Transkript();
            transkript.setSemester(data.getSemester());
            transkript.setMahasiswa(mahasiswa);
            transkript.setMatakuliah(m);
            transkript.setSks(data.getSks());
            transkript.setBobot(data.getBobot());
            transkript.setGrade(data.getGrade());
            transkriptDao.save(transkript);
        }
    }
}
