package id.ac.tazkia.smilemahasiswa.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.ac.tazkia.smilemahasiswa.dao.NoteDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.Note;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

@Service
public class GraduationService {
    @Autowired
    private NoteDao noteDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    public void downloadDataConceptNote(TahunAkademik tahunAkademik, Prodi prodi, HttpServletResponse response)
            throws IOException {

        String[] column = { "No", "Nim", "Nama", "Prodi", "Pembimbing 1", "Pembimbing 2", "Jenis" };
        List<Note> notes = noteDao.findByTahunAkademikAndMahasiswaIdProdiAndStatus(tahunAkademik, prodi,
                StatusApprove.APPROVED);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Concept Note");

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 20);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        titleFont.setFontName("Cambria");

        Font detailFont = workbook.createFont();
        detailFont.setFontHeightInPoints((short) 12);
        detailFont.setFontName("Arial");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerFont.setFontName("Cambria");

        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 10);
        dataFont.setFontName("Cambria");

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setFont(dataFont);

        CellStyle detailCellStyle = workbook.createCellStyle();
        detailCellStyle.setFont(detailFont);

        CellStyle titleCellStyle = workbook.createCellStyle();
        titleCellStyle.setFont(titleFont);
        titleCellStyle.setAlignment(HorizontalAlignment.LEFT);
        titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Row headerRow = sheet.createRow(1);

        for (int i = 0; i < column.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 2;
        int baris = 1;

        for (Note note : notes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(note.getMahasiswa().getNim());
            row.createCell(2).setCellValue(note.getMahasiswa().getNama());
            row.createCell(3).setCellValue(note.getMahasiswa().getIdProdi().getNamaProdi());
            row.createCell(4).setCellValue(note.getDosen().getKaryawan().getNamaKaryawan());
            if (note.getDosen2() != null) {
                row.createCell(5).setCellValue(note.getDosen2().getKaryawan().getNamaKaryawan());
            }

            String jenis = null;

            if (note.getJenis() == StatusRecord.SKRIPSI) {
                jenis = "Skripsi";

            }

            if (note.getJenis() == StatusRecord.JURNAL) {
                jenis = "Publikasi Jurnal";

            }

            if (note.getJenis() == StatusRecord.STUDI_KELAYAKAN) {
                jenis = "Studi Kelayakan Bisnis";

            }

            if (note.getJenis() == StatusRecord.MBKM) {
                jenis = "Magang MBKM";

            }

            if (note.getJenis() == StatusRecord.TESIS) {
                jenis = "Tesis";

            }

            row.createCell(5).setCellValue("-");
            row.createCell(6).setCellValue(jenis);
            row.getCell(0).setCellStyle(dataCellStyle);
            row.getCell(1).setCellStyle(dataCellStyle);
            row.getCell(2).setCellStyle(dataCellStyle);
            row.getCell(3).setCellStyle(dataCellStyle);
            row.getCell(4).setCellStyle(dataCellStyle);
            row.getCell(5).setCellStyle(dataCellStyle);
            row.getCell(6).setCellStyle(dataCellStyle);
        }

        for (int i = 0; i < column.length; i++) {
            sheet.autoSizeColumn(i);
        }

        String fileName = "Laporan Concept Note " + prodi.getNamaProdi() + ".xlsx";

        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + extentionX + "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public TahunAkademik cekTahunAkademik(TahunAkademik tahunAkademik) {
        TahunAkademik ta = null;

        if (tahunAkademik.getJenis() == StatusRecord.PENDEK) {
            String kode = tahunAkademik.getKodeTahunAkademik().substring(0, 4) + "2";
            ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS), kode);
        } else {
            if (LocalDate.now().compareTo(tahunAkademik.getTanggalMulai()) >= 0) {
                ta = tahunAkademik;
            } else {
                int last = tahunAkademik.getKodeTahunAkademik().length();
                String lastKode = String.valueOf(tahunAkademik.getKodeTahunAkademik().charAt(last - 1));

                if (lastKode.equals("1")) {
                    Integer tahun = Integer.valueOf(tahunAkademik.getTahun()) - 1;
                    String kode = String.valueOf(tahun) + "2";
                    ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),
                            kode);
                } else {
                    String kode = String.valueOf(tahunAkademik.getTahun()) + "1";
                    ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),
                            kode);
                }

            }
        }

        return ta;
    }
}
