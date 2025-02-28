package id.ac.tazkia.smilemahasiswa.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service @Slf4j
public class ReportService {

    @Value("${logo.surat}")
    private String logo;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

//    @Autowired
//    private TahunAkademikDao tahunAkademikDao;
//
//    public String[] selectedYear(String[] selectYear) {
//
//        List<String> idList = Arrays.asList(selectYear);
//
//        List<TahunAkademik> tahunAkademikList = tahunAkademikDao.findByIdIn(idList);
//
//        tahunAkademikList.sort(Comparator.comparing(TahunAkademik::getKodeTahunAkademik));
//
//        String[] column = new String[2 + tahunAkademikList.size()];
//        column[0] = "no";
//        column[1] = "pertanyaan";
//
//        for (int i = 0; i < tahunAkademikList.size(); i++) {
//            column[i + 2] = tahunAkademikList.get(i).getNamaTahunAkademik();
//        }
//
//        return column;
//    }

    public void downloadReport(Jadwal jadwal, HttpServletResponse response) throws IOException {

        String dosen = jadwalDosenDao.headerJadwal(jadwal.getId());

        // attandance
        int no = 1;
        List<Object[]> hasil = presensiMahasiswaDao.bkdAttendance(jadwal);
        log.debug("size attendance : {}", hasil);

        // topic
        int noTopic = 1;
        List<Object[]> topic = presensiDosenDao.bkdBeritaAcara(jadwal);
        log.debug("size topic : {}", topic);

        // score
        int noNilai = 1;
        List<Object[]> nilai = presensiMahasiswaDao.bkdNilai(jadwal);
        log.debug("size nilai : {}", nilai);

        Document document = new Document(PageSize.A3);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.defaultEncoding, 10, Font.BOLD);
        Paragraph newLine = new Paragraph("\n");

        Image lTazkia = Image.getInstance(logo + File.separator + "Tazkia Univ.png");
        lTazkia.scaleToFit(80, 80);
        lTazkia.setAbsolutePosition(50, PageSize.A3.getHeight() - (lTazkia.getScaledHeight()) - 25);
        document.add(lTazkia);

        // HALAMAN ATTENDANCE

        Paragraph title1 = new Paragraph(new Phrase("ATTENDANCE LIST", titleFont));
        title1.setAlignment(Element.ALIGN_CENTER);
        document.add(title1);

        Paragraph title2 = new Paragraph(new Phrase("TAZKIA ISLAMIC UNIVERSITY", titleFont));
        title2.setAlignment(Element.ALIGN_CENTER);
        document.add(title2);

        Paragraph title3 = new Paragraph(new Phrase("ACADEMIC YEAR " + jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0,9), titleFont));
        title3.setAlignment(Element.ALIGN_CENTER);
        document.add(title3);
        document.add(newLine);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new int[] {4, 1, 15});
        table.setSpacingBefore(15);
        Font font = FontFactory.getFont(FontFactory.defaultEncoding, 10);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("Department/Group",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getProdi().getNamaProdiEnglish() + " / " + jadwal.getKelas().getNamaKelas(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Subject",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Lecture",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(dosen, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        document.add(table);
        document.add(newLine);

        PdfPTable tableAbsen = new PdfPTable(19);
        tableAbsen.setWidthPercentage(100);
        tableAbsen.setWidths(new int[] {1, 4, 4, 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1});

        cell = new PdfPCell(new Phrase("No", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("NIM", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("Name Of Student", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("Class Meeting, Date & Sign", font));
        cell.setColspan(16);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("1", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("2", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("3", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("4", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("5", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("6", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("7", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("8", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("9", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("10", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("11", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("12", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("13", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("14", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("15", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase("16", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableAbsen.addCell(cell);
        for (Object[] list : hasil){

            // no
            cell = new PdfPCell(new Phrase(String.valueOf(no++), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            // nim
            cell = new PdfPCell(new Phrase(list[2].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            // nama
            cell = new PdfPCell(new Phrase(list[3].toString(),font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[4].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[5].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[6].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[7].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[8].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[9].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[10].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[11].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[12].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[13].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[14].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[15].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[16].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[17].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[18].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
            cell = new PdfPCell(new Phrase(list[19].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableAbsen.addCell(cell);
        }

        cell = new PdfPCell(new Phrase("Sign Of Lecture:", titleFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setColspan(3);
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);
        cell = new PdfPCell(new Phrase());
        tableAbsen.addCell(cell);

        document.add(tableAbsen);

        document.add(newLine);

        Paragraph jabatan = new Paragraph(jadwal.getProdi().getJabatan(), font);
        document.add(jabatan);
        document.add(newLine);
        document.add(newLine);

        Paragraph kps = new Paragraph(jadwal.getProdi().getDosen().getKaryawan().getNamaKaryawan(), font);
        document.add(kps);

        // HALAMAN BERITA ACARA/TOPIC

        document.newPage();

        document.add(lTazkia);

        Paragraph title4 = new Paragraph(new Phrase("CLASS MEETING REPORT", titleFont));
        title4.setAlignment(Element.ALIGN_CENTER);
        document.add(title4);

        Paragraph title5 = new Paragraph(new Phrase("TAZKIA ISLAMIC UNIVERSITY", titleFont));
        title5.setAlignment(Element.ALIGN_CENTER);
        document.add(title5);

        Paragraph title6 = new Paragraph(new Phrase("ACADEMIC YEAR " + jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0,9), titleFont));
        title6.setAlignment(Element.ALIGN_CENTER);
        document.add(title6);
        document.add(newLine);

        PdfPTable table2 = new PdfPTable(3);
        table2.setWidthPercentage(100);
        table2.setWidths(new int[] {4, 1, 15});
        table2.setSpacingBefore(15);
        font.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Department/Group",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getProdi().getNamaProdiEnglish() + " / " + jadwal.getKelas().getNamaKelas(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase("Subject",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase("Lecture",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(dosen, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table2.addCell(cell);

        document.add(table2);
        document.add(newLine);

        PdfPTable topicTable = new PdfPTable(10);
        topicTable.setWidthPercentage(100);
        topicTable.setWidths(new int[] {2, 5, 4, 4, 10, 5, 5, 10, 4, 4});

        cell = new PdfPCell(new Phrase("NO", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("DATE", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("TIME IN", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("TIME OUT", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("TOPIC", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("ASSIGNMENT", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("STUDENT WHO DON'T ATTEND", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("LECTURE", font));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("SIGN", font));
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("LECTUTE", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);
        cell = new PdfPCell(new Phrase("STUDENT", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        topicTable.addCell(cell);

        for (Object[] list : topic){

            // no
            cell = new PdfPCell(new Phrase(String.valueOf(noTopic++), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            // date
            cell = new PdfPCell(new Phrase(list[0].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            // time in
            cell = new PdfPCell(new Phrase(list[1].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            // time out
            cell = new PdfPCell(new Phrase(list[2].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            // topic
            cell = new PdfPCell(new Phrase(list[3].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            // assignment
            cell = new PdfPCell(new Phrase(list[4].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            // student who dont attend
            cell = new PdfPCell(new Phrase(list[5].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            // lecture
            cell = new PdfPCell(new Phrase(list[6].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            topicTable.addCell(cell);
            cell = new PdfPCell(new Phrase());
            topicTable.addCell(cell);
            cell = new PdfPCell(new Phrase());
            topicTable.addCell(cell);

        }

        document.add(topicTable);

        document.add(newLine);

        document.add(jabatan);
        document.add(newLine);
        document.add(newLine);
        document.add(kps);

        // HALAMAN NILAI/SCORE
        document.newPage();

        document.add(lTazkia);

        Font fontBold = FontFactory.getFont(FontFactory.defaultEncoding, 10, Font.BOLD);
        Font fontItalic = FontFactory.getFont(FontFactory.defaultEncoding, 10, Font.ITALIC);

        Paragraph title7 = new Paragraph(new Phrase("TAZKIA ISLAMIC UNIVERSITY", titleFont));
        title7.setAlignment(Element.ALIGN_CENTER);
        document.add(title7);

        Paragraph title8 = new Paragraph(new Phrase("ACADEMIC YEAR " + jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0,9), titleFont));
        title8.setAlignment(Element.ALIGN_CENTER);
        document.add(title8);

        Paragraph title9 = new Paragraph(new Phrase("Telp. +62-21-87962291-93, Fax. +62-21-87962294", fontItalic));
        title9.setAlignment(Element.ALIGN_CENTER);
        document.add(title9);
        document.add(newLine);
        document.add(newLine);

        Paragraph nilaiMatakuliah = new Paragraph("Nilai Mata Kuliah Mahasiswa", fontBold);
        nilaiMatakuliah.setAlignment(Element.ALIGN_CENTER);
        document.add(nilaiMatakuliah);
        document.add(newLine);

        PdfPTable headerTableTopic = new PdfPTable(6);
        headerTableTopic.setWidthPercentage(80);
        headerTableTopic.setWidths(new int[] {5,1,10,5,1,10});

        cell = new PdfPCell(new Phrase("Mata Kuliah", fontItalic));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(":", fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish(), fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase("Dosen Pengasuh", fontItalic));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(":", fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(dosen, fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase("Kelas / Thn Akd", fontItalic));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(":", fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getKelas().getNamaKelas() + " / " + jadwal.getTahunAkademik().getNamaTahunAkademik(), fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase("Program Studi", fontItalic));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(":", fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getProdi().getNamaProdi(), fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase("Semester / SKS", fontItalic));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(":", fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getMatakuliahKurikulum().getSemester() + " / " + jadwal.getMatakuliahKurikulum().getJumlahSks(), fontBold));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase());
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase());
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);
        cell = new PdfPCell(new Phrase());
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTableTopic.addCell(cell);

        document.add(headerTableTopic);

        document.add(newLine);

        PdfPTable tableNilai = new PdfPTable(6);
        tableNilai.setWidthPercentage(100);
        tableNilai.setWidths(new int[] {2, 5, 10, 5, 5, 5});

        cell = new PdfPCell(new Phrase("NO. ", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Nomer Induk Mahasiswa", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Nama Mahasiswa", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Nilai Akhir", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Grade Nilai", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableNilai.addCell(cell);
        cell = new PdfPCell(new Phrase("Bobot Nilai", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableNilai.addCell(cell);
        for (Object[] list : nilai){

            cell = new PdfPCell(new Phrase(String.valueOf(noNilai++), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableNilai.addCell(cell);
            cell = new PdfPCell(new Phrase(list[0].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableNilai.addCell(cell);
            cell = new PdfPCell(new Phrase(list[1].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableNilai.addCell(cell);
            cell = new PdfPCell(new Phrase(list[2].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableNilai.addCell(cell);
            cell = new PdfPCell(new Phrase(list[3].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableNilai.addCell(cell);
            cell = new PdfPCell(new Phrase(list[4].toString(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableNilai.addCell(cell);

        }

        document.add(tableNilai);

        Paragraph tgl = new Paragraph("Bogor, " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), font);
        tgl.setIndentationLeft(2);
        document.add(tgl);

        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);
        footer.setWidths(new int[] {15,15});

        cell = new PdfPCell(new Phrase(jadwal.getProdi().getJabatan(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        footer.addCell(cell);
        cell = new PdfPCell(new Phrase("Dosen Pengasuh, ", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footer.addCell(cell);
        cell = new PdfPCell(new Phrase());
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(25);
        footer.addCell(cell);
        cell = new PdfPCell(new Phrase());
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(25);
        footer.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getProdi().getDosen().getKaryawan().getNamaKaryawan(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        footer.addCell(cell);
        cell = new PdfPCell(new Phrase(jadwal.getDosen().getKaryawan().getNamaKaryawan(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footer.addCell(cell);
        cell = new PdfPCell(new Phrase());
        cell.setBorder(Rectangle.NO_BORDER);
        footer.addCell(cell);
        cell = new PdfPCell(new Phrase("NIDN : " + jadwal.getDosen().getKaryawan().getNidn(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footer.addCell(cell);

        document.add(footer);

        document.close();

    }

}
