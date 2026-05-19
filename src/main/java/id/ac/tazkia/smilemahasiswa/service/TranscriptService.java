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

    @Value("${logo.ttd}")
    private String logoTtd;

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

        Image lTazkia = Image.getInstance(logo + File.separator + "Tazkia Univ.png");
        lTazkia.scaleToFit(60, 60);
        lTazkia.setAbsolutePosition(80, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 45);

        document.add(lTazkia);

        Paragraph tazkia = new Paragraph("UNIVERSITAS ISLAM TAZKIA", titleFont);
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

        Image lTazkia = Image.getInstance(logo + File.separator + "Tazkia Univ.png");
        lTazkia.scaleToFit(60, 60);
        lTazkia.setAbsolutePosition(80, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 45);

        document.add(lTazkia);

        Paragraph tazkia = new Paragraph("UNIVERSITAS ISLAM TAZKIA", titleFont);
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

        String ttdFileName = mhs.getIdProdi().getDosen().getKaryawan().getTandaTangan();
        if (ttdFileName != null && !ttdFileName.isEmpty()) {
            File ttdFile = new File(logoTtd + File.separator + ttdFileName);
            if (ttdFile.exists()) {
                Image ttdImage = Image.getInstance(ttdFile.getAbsolutePath());
                ttdImage.scaleToFit(150, 80);
                document.add(ttdImage);
            } else {
                document.add(nLine);
                document.add(nLine);
            }
        } else {
            document.add(nLine);
            document.add(nLine);
        }

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

        Image lTazkia = Image.getInstance(logo + File.separator + "Tazkia Univ.png");
        lTazkia.scaleToFit(60, 60);
        lTazkia.setAbsolutePosition(80, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 45);

        document.add(lTazkia);

        Paragraph tazkia = new Paragraph("TAZKIA ISLAMIC UNIVERSITY", titleFont);
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

    public void transkripIndo2(Mahasiswa mhs, List<DataTranskript> semester1, List<DataTranskript> semester2,
                               List<DataTranskript> semester3, List<DataTranskript> semester4,
                               List<DataTranskript> semester5, List<DataTranskript> semester6,
                               List<DataTranskript> semester7, List<DataTranskript> semester8,
                               int sks, BigDecimal ipk, BigDecimal mutu,
                               HttpServletResponse response) throws IOException {

        Document document = new Document(PageSize.A4, 50, 50, 30, 30);
        PdfWriter writer2 = PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        FontFactory.registerDirectories();

        java.awt.Color brandColor = new java.awt.Color(21, 53, 113);
        Font kemenFont = FontFactory.getFont("Arial", 12f, Font.BOLD, brandColor);
        Font uniFont = FontFactory.getFont("Arial", 16f, Font.BOLD, brandColor);
        Font transFontBold = FontFactory.getFont("Arial", 12f, Font.BOLD, brandColor);
        Font nomorFont = FontFactory.getFont("Times New Roman", 10f);

        // Load Cambria dari classpath (bundled di resources/fonts/)
        byte[] cambriaBytes = getClass().getClassLoader().getResourceAsStream("fonts/Cambria.ttc").readAllBytes();
        byte[] cambriaBoldBytes = getClass().getClassLoader().getResourceAsStream("fonts/Cambriab.ttf").readAllBytes();
        byte[] cambriaItalicBytes = getClass().getClassLoader().getResourceAsStream("fonts/Cambriai.ttf").readAllBytes();
        byte[] cambriaBoldItalicBytes = getClass().getClassLoader().getResourceAsStream("fonts/Cambriaz.ttf").readAllBytes();
        BaseFont bfCambria = BaseFont.createFont("Cambria.ttc,1", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, cambriaBytes, null);
        BaseFont bfCambriaBold = BaseFont.createFont("Cambriab.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, cambriaBoldBytes, null);
        BaseFont bfCambriaItalic = BaseFont.createFont("Cambriai.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, cambriaItalicBytes, null);
        BaseFont bfCambriaBoldItalic = BaseFont.createFont("Cambriaz.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, cambriaBoldItalicBytes, null);
        Font cambria = new Font(bfCambria, 10f);
        Font cambriaBold = new Font(bfCambriaBold, 10f);
        Font cambriaItalic = new Font(bfCambriaItalic, 10f);
        Font cambriaBoldItalic = new Font(bfCambriaBoldItalic, 10f);
        // aliases untuk kompatibilitas kode di bawah
        Font boldFont = cambriaBold;
        Font normalFont = cambria;
        Font smallFont = cambria;
        Font smallBoldFont = cambriaBold;
        Font italicSmallFont = cambriaItalic;
        Font matkulFont = cambria;
        Font matkulBoldFont = cambriaBold;

        // ===== HEADER =====
        java.net.URL logoUrl = getClass().getClassLoader().getResource("tazkia3.png");
        Image logo2 = Image.getInstance(logoUrl);
        logo2.scaleToFit(100, 100);
        PdfPTable logoTable = new PdfPTable(1);
        logoTable.setWidthPercentage(100);
        PdfPCell logoCell = new PdfPCell(logo2, false);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPaddingBottom(2);
        logoTable.addCell(logoCell);
        document.add(logoTable);

        Paragraph kemen = new Paragraph("KEMENTERIAN AGAMA REPUBLIK INDONESIA", kemenFont);
        kemen.setAlignment(Element.ALIGN_CENTER);
        kemen.setSpacingAfter(0);
        document.add(kemen);

        Paragraph uni = new Paragraph("UNIVERSITAS ISLAM TAZKIA", uniFont);
        uni.setAlignment(Element.ALIGN_CENTER);
        uni.setSpacingAfter(0);
        document.add(uni);

        Paragraph transLabel = new Paragraph("TRANSKRIP AKADEMIK", transFontBold);
        transLabel.setAlignment(Element.ALIGN_CENTER);
        transLabel.setSpacingAfter(0);
        document.add(transLabel);

        String noTranskript = mhs.getNoTranskript() != null ? mhs.getNoTranskript() : "-";
        Paragraph nomorPar = new Paragraph("Nomor : " + noTranskript, nomorFont);
        nomorPar.setAlignment(Element.ALIGN_CENTER);
        nomorPar.setSpacingAfter(0);
        document.add(nomorPar);

        // Garis pembatas header
        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        lineTable.setSpacingBefore(2);
        PdfPCell lineCell = new PdfPCell(new Phrase(""));
        lineCell.setBorder(Rectangle.NO_BORDER);
        lineCell.setBorderWidthBottom(1f);
        lineCell.setPaddingBottom(3);
        lineTable.addCell(lineCell);
        document.add(lineTable);

        // ===== INFO MAHASISWA =====
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(80);
        infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        infoTable.setWidths(new float[]{4, 6});
        infoTable.setSpacingBefore(3);
        PdfPCell ic;

        String programPendidikan = "Sarjana";
        if (mhs.getIdProdi().getIdJenjang() != null) {
            String idJenjang = mhs.getIdProdi().getIdJenjang().getId();
            if ("02".equals(idJenjang)) programPendidikan = "Magister";
            else if ("03".equals(idJenjang)) programPendidikan = "Doktor";
        }

        String tglLahir = mhs.getTempatLahir() != null ? mhs.getTempatLahir() : "";
        if (mhs.getTanggalLahir() != null) tglLahir += ", " + convertDateIndonesia(mhs.getTanggalLahir());

        String tglLulus = mhs.getTanggalLulus() != null ? convertDateIndonesia(mhs.getTanggalLulus()) : "-";
        String noAkreditasi = mhs.getIdProdi().getNoSk() != null ? mhs.getIdProdi().getNoSk() : "-";
        String tglSk = mhs.getIdProdi().getTanggalSk() != null ? convertDateIndonesia(mhs.getIdProdi().getTanggalSk()) : "-";
        String indukNasional = mhs.getIndukNasional() != null ? mhs.getIndukNasional() : "-";

        String[][] infoRows = {
            {"Nama Mahasiswa", mhs.getNama()},
            {"NIM", mhs.getNim()},
            {"Nomor Ijazah Nasional", indukNasional},
            {"Tempat, Tanggal lahir", tglLahir},
            {"Program Pendidikan", programPendidikan},
            {"Program Studi", mhs.getIdProdi().getNamaProdi()},
            {"Fakultas", mhs.getIdProdi().getFakultas().getNamaFakultas()},
            {"Nomor Akreditasi", noAkreditasi},
            {"Tanggal SK Akreditasi", tglSk},
            {"Tanggal Kelulusan", tglLulus}
        };
        for (String[] row : infoRows) {
            ic = new PdfPCell(new Phrase(row[0], normalFont)); ic.setBorder(Rectangle.NO_BORDER); ic.setPaddingTop(1); ic.setPaddingBottom(1); ic.setPaddingLeft(2); ic.setPaddingRight(2); infoTable.addCell(ic);
            ic = new PdfPCell(new Phrase(": " + row[1], normalFont)); ic.setBorder(Rectangle.NO_BORDER); ic.setPaddingTop(1); ic.setPaddingBottom(1); ic.setPaddingLeft(2); ic.setPaddingRight(2); infoTable.addCell(ic);
        }
        document.add(infoTable);

        // ===== TABEL NILAI (tanpa kolom Semester) =====
        // Kode Mata Kuliah | Mata Kuliah | Satuan Kredit | Huruf Mutu | Angka Mutu | Jumlah
        PdfPTable tabNilai = new PdfPTable(6);
        tabNilai.setWidthPercentage(100);
        tabNilai.setWidths(new float[]{2.5f, 10, 2, 2, 2, 2});
        tabNilai.setSpacingBefore(12);

        String[] headers = {"Kode Mata\nKuliah", "Mata Kuliah", "Satuan\nKredit", "Huruf\nMutu", "Angka\nMutu", "Jumlah"};
        for (String h : headers) {
            PdfPCell hc = new PdfPCell(new Phrase(h, matkulBoldFont));
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            hc.setVerticalAlignment(Element.ALIGN_MIDDLE);
            hc.setPadding(3);
            hc.setBorderWidthTop(1.5f); hc.setBorderWidthBottom(1.5f);
            hc.setBorderWidthLeft(0.5f); hc.setBorderWidthRight(0.5f);
            tabNilai.addCell(hc);
        }
        tabNilai.setHeaderRows(1); // repeat header on each new page

        // Garis penutup bawah saat tabel pindah ke halaman berikutnya
        tabNilai.setTableEvent((table, widths, heights, headerRows1, rowStart, canvases) -> {
            int rowsOnPage = heights.length - 1;
            boolean tableComplete = (rowStart + rowsOnPage - headerRows1) >= table.size();
            if (!tableComplete) {
                PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
                float y = heights[heights.length - 1];
                float x1 = widths[0][0];
                float x2 = widths[0][widths[0].length - 1];
                cb.setLineWidth(1.0f);
                cb.moveTo(x1, y);
                cb.lineTo(x2, y);
                cb.stroke();
            }
        });

        List<List<DataTranskript>> semesterList = java.util.Arrays.asList(
            semester1, semester2, semester3, semester4,
            semester5, semester6, semester7, semester8);

        boolean firstSem = true;
        for (List<DataTranskript> sem : semesterList) {
            if (sem == null || sem.isEmpty()) continue;
            boolean firstRow = true;
            for (DataTranskript data : sem) {
                // Garis pemisah antar semester: baris pertama setiap semester (kecuali semester pertama) punya top border
                float topBorder = (!firstSem && firstRow) ? 0.8f : 0f;
                PdfPCell dc;
                dc = new PdfPCell(new Phrase(data.getKode(), matkulFont)); dc.setHorizontalAlignment(Element.ALIGN_CENTER); dc.setPadding(2); dc.setBorderWidthLeft(0.5f); dc.setBorderWidthRight(0.5f); dc.setBorderWidthTop(topBorder); dc.setBorderWidthBottom(0f); tabNilai.addCell(dc);
                dc = new PdfPCell(new Phrase(data.getMatkul(), matkulFont)); dc.setPadding(2); dc.setBorderWidthLeft(0.5f); dc.setBorderWidthRight(0.5f); dc.setBorderWidthTop(topBorder); dc.setBorderWidthBottom(0f); tabNilai.addCell(dc);
                dc = new PdfPCell(new Phrase(data.getSks().toString(), matkulFont)); dc.setHorizontalAlignment(Element.ALIGN_CENTER); dc.setPadding(2); dc.setBorderWidthLeft(0.5f); dc.setBorderWidthRight(0.5f); dc.setBorderWidthTop(topBorder); dc.setBorderWidthBottom(0f); tabNilai.addCell(dc);
                dc = new PdfPCell(new Phrase(data.getGrade(), matkulFont)); dc.setHorizontalAlignment(Element.ALIGN_CENTER); dc.setPadding(2); dc.setBorderWidthLeft(0.5f); dc.setBorderWidthRight(0.5f); dc.setBorderWidthTop(topBorder); dc.setBorderWidthBottom(0f); tabNilai.addCell(dc);
                dc = new PdfPCell(new Phrase(data.getBobot().toString(), matkulFont)); dc.setHorizontalAlignment(Element.ALIGN_CENTER); dc.setPadding(2); dc.setBorderWidthLeft(0.5f); dc.setBorderWidthRight(0.5f); dc.setBorderWidthTop(topBorder); dc.setBorderWidthBottom(0f); tabNilai.addCell(dc);
                dc = new PdfPCell(new Phrase(data.getMutu().toString(), matkulFont)); dc.setHorizontalAlignment(Element.ALIGN_CENTER); dc.setPadding(2); dc.setBorderWidthLeft(0.5f); dc.setBorderWidthRight(0.5f); dc.setBorderWidthTop(topBorder); dc.setBorderWidthBottom(0f); tabNilai.addCell(dc);
                firstRow = false;
            }
            firstSem = false;
        }

        // Row Jumlah
        PdfPCell tc;
        tc = new PdfPCell(new Phrase("Jumlah", matkulBoldFont)); tc.setColspan(2); tc.setHorizontalAlignment(Element.ALIGN_CENTER); tc.setPadding(3); tc.setBorderWidthTop(1.5f); tc.setBorderWidthBottom(1.5f); tc.setBorderWidthLeft(0.5f); tc.setBorderWidthRight(0.5f); tabNilai.addCell(tc);
        tc = new PdfPCell(new Phrase(String.valueOf(sks), matkulBoldFont)); tc.setHorizontalAlignment(Element.ALIGN_CENTER); tc.setPadding(3); tc.setBorderWidthTop(1.5f); tc.setBorderWidthBottom(1.5f); tc.setBorderWidthLeft(0.5f); tc.setBorderWidthRight(0.5f); tabNilai.addCell(tc);
        tc = new PdfPCell(new Phrase("")); tc.setColspan(2); tc.setPadding(3); tc.setBorderWidthTop(1.5f); tc.setBorderWidthBottom(1.5f); tc.setBorderWidthLeft(0.5f); tc.setBorderWidthRight(0.5f); tabNilai.addCell(tc);
        tc = new PdfPCell(new Phrase(mutu.toString(), matkulBoldFont)); tc.setHorizontalAlignment(Element.ALIGN_CENTER); tc.setPadding(3); tc.setBorderWidthTop(1.5f); tc.setBorderWidthBottom(1.5f); tc.setBorderWidthLeft(0.5f); tc.setBorderWidthRight(0.5f); tabNilai.addCell(tc);

        document.add(tabNilai);

        // Tabel bantu tanpa border: label IPK di kolom 1-2, nilai IPK sejajar kolom SKS (kolom 3)
        PdfPTable ipkAlignTable = new PdfPTable(6);
        ipkAlignTable.setWidthPercentage(100);
        ipkAlignTable.setWidths(new float[]{2.5f, 10, 2, 2, 2, 2});
        ipkAlignTable.setSpacingBefore(0);
        PdfPCell ia;
        ia = new PdfPCell(new Phrase("Indeks Prestasi Kumulatif", cambriaBold)); ia.setColspan(2); ia.setBorder(Rectangle.NO_BORDER); ia.setPadding(3); ipkAlignTable.addCell(ia);
        ia = new PdfPCell(new Phrase(ipk.toString(), cambria)); ia.setBorder(Rectangle.NO_BORDER); ia.setHorizontalAlignment(Element.ALIGN_CENTER); ia.setPadding(3); ipkAlignTable.addCell(ia);
        ia = new PdfPCell(new Phrase("")); ia.setColspan(3); ia.setBorder(Rectangle.NO_BORDER); ia.setPadding(3); ipkAlignTable.addCell(ia);
        document.add(ipkAlignTable);
        document.add(new Paragraph(" "));

        // ===== IPK & PREDIKAT =====
        String predicate;
        Integer validate = krsDetailDao.validasiPredikat(mhs);
        if (ipk.compareTo(new BigDecimal("3.81")) >= 0 && validate.equals(0)) {
            predicate = "Pujian Tertinggi";
        } else if (ipk.compareTo(new BigDecimal("3.51")) >= 0 && validate.equals(0)) {
            predicate = "Pujian";
        } else if (ipk.compareTo(new BigDecimal("3.01")) >= 0) {
            predicate = "Sangat Memuaskan";
        } else {
            predicate = "Memuaskan";
        }

        Paragraph predPar = new Paragraph("Predikat :  " + predicate, boldFont);
        document.add(predPar);

        if (mhs.getJudul() != null && !mhs.getJudul().isEmpty()) {
            String labelJudul = "Judul Skripsi :  ";
            if (mhs.getIdProdi().getIdJenjang() != null && "02".equals(mhs.getIdProdi().getIdJenjang().getId())) {
                labelJudul = "Judul Tesis :  ";
            }
            Phrase judulPhrase = new Phrase();
            judulPhrase.add(new Chunk(labelJudul, cambriaBold));
            judulPhrase.add(new Chunk(mhs.getJudul(), normalFont));
            Paragraph judulPar = new Paragraph(judulPhrase);
            judulPar.setSpacingBefore(2);
            document.add(judulPar);
        }
        document.add(new Paragraph(" "));

        // ===== TABEL PRESTASI AKADEMIK + SISTEM PENILAIAN =====
        // Tabel Prestasi Akademik
        PdfPTable prestasiTable = new PdfPTable(2);
        prestasiTable.setTotalWidth(new float[]{50f, 160f});
        prestasiTable.setLockedWidth(true);
        PdfPCell ph = new PdfPCell(new Phrase("Prestasi Akademik", smallBoldFont));
        ph.setColspan(2); ph.setHorizontalAlignment(Element.ALIGN_CENTER); ph.setPadding(3); ph.setBorderWidth(1.5f);
        prestasiTable.addCell(ph);
        ph = new PdfPCell(new Phrase("IPK", smallBoldFont));
        ph.setHorizontalAlignment(Element.ALIGN_CENTER); ph.setPadding(2); ph.setBorderWidth(1.5f);
        prestasiTable.addCell(ph);
        ph = new PdfPCell(new Phrase("Keterangan", smallBoldFont));
        ph.setHorizontalAlignment(Element.ALIGN_CENTER); ph.setPadding(2); ph.setBorderWidth(1.5f);
        prestasiTable.addCell(ph);
        String[][] prestasiRows = {
            {"3,81-4,00", "Pujian Tertinggi (Minimal B)"},
            {"3,51-3,80", "Pujian (Minimal B)"},
            {"3,01-3,50", "Sangat Memuaskan"},
            {"2,75-3,00", "Memuaskan"}
        };
        for (String[] row : prestasiRows) {
            PdfPCell rc = new PdfPCell(new Phrase(row[0], smallFont)); rc.setPadding(2); rc.setBorderWidth(1.5f); prestasiTable.addCell(rc);
            rc = new PdfPCell(new Phrase(row[1], smallFont)); rc.setPadding(2); rc.setBorderWidth(1.5f); prestasiTable.addCell(rc);
        }

        // Tabel Sistem Penilaian
        PdfPTable penilaianTable = new PdfPTable(3);
        penilaianTable.setWidths(new float[]{1.5f, 1.5f, 4});
        PdfPCell pph = new PdfPCell(new Phrase("Sistem Penilaian", smallBoldFont));
        pph.setColspan(3); pph.setHorizontalAlignment(Element.ALIGN_CENTER); pph.setPadding(3); pph.setBorderWidth(1.5f);
        penilaianTable.addCell(pph);
        String[] pHeaders = {"HM", "AM", "Arti"};
        for (String h : pHeaders) {
            Chunk hChunk = new Chunk(h, smallBoldFont); hChunk.setUnderline(0.5f, -1.5f);
            PdfPCell hc = new PdfPCell(new Phrase(hChunk)); hc.setHorizontalAlignment(Element.ALIGN_CENTER); hc.setPadding(2); hc.setBorder(Rectangle.NO_BORDER);
            penilaianTable.addCell(hc);
        }
        String[][] penilaianRows = {
            {"A", "4", "Baik Sekali"},
            {"A-", "3,7", "Baik Sekali"},
            {"B+", "3,3", "Baik"},
            {"B", "3", "Baik"},
            {"B-", "2,7", "Baik"},
            {"C+", "2,3", "Cukup"},
            {"C", "2", "Cukup"},
            {"D", "1", "Kurang"},
            {"E", "0", "Sangat Kurang"}
        };
        for (String[] row : penilaianRows) {
            PdfPCell rc;
            rc = new PdfPCell(new Phrase(row[0], smallFont)); rc.setHorizontalAlignment(Element.ALIGN_CENTER); rc.setPadding(2); rc.setBorder(Rectangle.NO_BORDER); penilaianTable.addCell(rc);
            rc = new PdfPCell(new Phrase(row[1], smallFont)); rc.setHorizontalAlignment(Element.ALIGN_CENTER); rc.setPadding(2); rc.setBorder(Rectangle.NO_BORDER); penilaianTable.addCell(rc);
            rc = new PdfPCell(new Phrase(row[2], italicSmallFont)); rc.setHorizontalAlignment(Element.ALIGN_CENTER); rc.setPadding(2); rc.setBorder(Rectangle.NO_BORDER); penilaianTable.addCell(rc);
        }

        // Gunakan ColumnText agar border prestasiTable tidak stretch
        float pageWidth2 = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        float prestasiWidth2 = 210f;
        float gap2 = 15f;
        float penilaianWidth2 = pageWidth2 - prestasiWidth2 - gap2;
        float curY2 = writer2.getVerticalPosition(true);
        float colBottom2 = document.bottomMargin();

        com.lowagie.text.pdf.ColumnText ct2 = new com.lowagie.text.pdf.ColumnText(writer2.getDirectContent());
        ct2.setSimpleColumn(document.leftMargin(), colBottom2,
                document.leftMargin() + prestasiWidth2, curY2);
        ct2.addElement(prestasiTable);
        ct2.go();

        com.lowagie.text.pdf.ColumnText ct3 = new com.lowagie.text.pdf.ColumnText(writer2.getDirectContent());
        ct3.setSimpleColumn(document.leftMargin() + prestasiWidth2 + gap2, colBottom2,
                document.leftMargin() + prestasiWidth2 + gap2 + penilaianWidth2, curY2);
        ct3.addElement(penilaianTable);
        ct3.go();

        // Hitung tinggi penilaianTable (lebih tinggi) lalu geser posisi dokumen
        penilaianTable.setTotalWidth(penilaianWidth2);
        float tablesHeight2 = penilaianTable.getTotalHeight();
        document.add(new com.lowagie.text.pdf.PdfPTable(1) {{
            setWidthPercentage(100);
            setSpacingBefore(0);
            PdfPCell spacer = new PdfPCell(new Phrase(""));
            spacer.setBorder(Rectangle.NO_BORDER);
            spacer.setFixedHeight(tablesHeight2);
            addCell(spacer);
        }});
        document.add(new Paragraph(" "));

        // ===== KALIMAT PENGESAHAN + TTD =====
        LocalDate tglDisahkan = mhs.getTanggalDisahkan() != null ? mhs.getTanggalDisahkan() : LocalDate.now();
        String tanggalDisahkan = convertDateIndonesia(tglDisahkan);
        java.time.chrono.HijrahDate hijrahDate = java.time.chrono.HijrahDate.from(tglDisahkan);
        String hijriDay = hijrahDate.format(java.time.format.DateTimeFormatter.ofPattern("d", new java.util.Locale("en")));
        String hijriMonth = hijrahDate.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", new java.util.Locale("en")));
        String hijriYear = hijrahDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy", new java.util.Locale("en")));
        String hijriMonthIndo;
        if (hijriMonth.equals("Muharram")) hijriMonthIndo = "Muharram";
        else if (hijriMonth.equals("Safar")) hijriMonthIndo = "Safar";
        else if (hijriMonth.equals("Rabiʻ I")) hijriMonthIndo = "Rabi'ul Awal";
        else if (hijriMonth.equals("Rabiʻ II")) hijriMonthIndo = "Rabi'ul Akhir";
        else if (hijriMonth.equals("Jumada I")) hijriMonthIndo = "Jumadil Awal";
        else if (hijriMonth.equals("Jumada II")) hijriMonthIndo = "Jumadil Akhir";
        else if (hijriMonth.equals("Rajab")) hijriMonthIndo = "Rajab";
        else if (hijriMonth.equals("Shaʻban")) hijriMonthIndo = "Sya'ban";
        else if (hijriMonth.equals("Ramadan")) hijriMonthIndo = "Ramadhan";
        else if (hijriMonth.equals("Shawwal")) hijriMonthIndo = "Syawwal";
        else if (hijriMonth.equals("Dhuʻl-Qiʻdah")) hijriMonthIndo = "Dzulqa'dah";
        else if (hijriMonth.equals("Dhuʻl-Hijjah")) hijriMonthIndo = "Dzulhijjah";
        else hijriMonthIndo = hijriMonth;
        String tanggalLengkap = tanggalDisahkan + " / " + hijriDay + " " + hijriMonthIndo + " " + hijriYear + " H";
        Paragraph tglPar = new Paragraph(
            "Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + tanggalLengkap, normalFont);
        document.add(tglPar);
        document.add(new Paragraph(" "));

        // TTD Dekan di kanan
        PdfPTable ttdTable = new PdfPTable(2);
        ttdTable.setWidthPercentage(100);
        ttdTable.setWidths(new float[]{1, 1});

        PdfPCell emptyCell = new PdfPCell(new Phrase("")); emptyCell.setBorder(Rectangle.NO_BORDER); ttdTable.addCell(emptyCell);

        PdfPCell ttdCell = new PdfPCell();
        ttdCell.setBorder(Rectangle.NO_BORDER);
        ttdCell.addElement(new Paragraph("Dekan", normalFont));
        ttdCell.addElement(new Paragraph(mhs.getIdProdi().getFakultas().getNamaFakultas(), normalFont));
        ttdCell.addElement(new Paragraph(" "));
        ttdCell.addElement(new Paragraph(" "));
        ttdCell.addElement(new Paragraph(" "));
        Chunk dosenChunk = new Chunk(mhs.getIdProdi().getFakultas().getDosen().getKaryawan().getNamaKaryawan(), boldFont);
        dosenChunk.setUnderline(0.5f, -1.5f);
        Paragraph dosenPar = new Paragraph(); dosenPar.add(dosenChunk);
        ttdCell.addElement(dosenPar);
        ttdCell.addElement(new Paragraph("NIK : " + mhs.getIdProdi().getFakultas().getDosen().getKaryawan().getNik(), boldFont));
        ttdTable.addCell(ttdCell);

        document.add(ttdTable);
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
